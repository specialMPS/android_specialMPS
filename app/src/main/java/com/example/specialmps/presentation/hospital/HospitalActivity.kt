package com.example.specialmps.presentation.hospital

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.specialmps.R
import com.example.specialmps.data.HospitalInfo
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_hospital.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.select.Elements
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder

class HospitalActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener {

    var fusedLocationClient : FusedLocationProviderClient?=null
    var locationCallback : LocationCallback?=null
    var locationRequest : com.google.android.gms.location.LocationRequest?=null

    lateinit var googlemap: GoogleMap

    var searchKeyword : String = ""
    var loc = LatLng(37.5552002, 126.9706291)

    val key1 = "b17ee37aae43497d8c94690258a08512"
    val key2 = "8072387393bf4a9f969c353ed2ad845b"

    val hospitalSearchUrl = "https://openapi.gg.go.kr/MindHealthPromotionCenter?KEY="+key1+"&Type=xml&pSize=200&"
    val hospitalURL = "https://openapi.gg.go.kr/MindHealthPromotionCenter?KEY="+key1+"&Type=xml&pSize=200&pIndex="
    val hospitalURL2 = " https://openapi.gg.go.kr/Ggmindmedinst"

    var arr : ArrayList<HospitalInfo> = ArrayList() //Marker에 넣을 병원 정보

    companion object{
        private val markerIconSize = 100
    }

    lateinit var markerIcon : Bitmap
    //private val hospitalManager by lazy { HospitalManager()} // 나중에 초기화 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital)
        map_linearlayout.visibility = View.GONE
        markerIcon =
            BitmapUtils.resizeMapIcons(this, R.drawable.marker_icon, markerIconSize, markerIconSize)
        init()
    }
    fun init(){
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //허용한 경우
            getuserlocation()
            startLocationUpdates()
            initMap()

        }
        else{
            //허용되지 않았을 때 권한 요청
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }

        search_map_button.setOnClickListener {
            softkeyboardhide() //키보드 내리기
            map_linearlayout.visibility = View.GONE //마커 띄워져 있는 경우 없애기

            searchKeyword = search_map.text.toString()
            if(searchKeyword == ""){
                Toast.makeText(this, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                //arr.clear()
                //initHospital()
                return@setOnClickListener
            }
            search_map_button.isClickable = false
            Toast.makeText(this, "잠시 기다려주세요!", Toast.LENGTH_SHORT).show()
            val check = searchKeyword[searchKeyword.length-1]
            if( check == '시' || check == '군'){//도시이름인 경우
                startTask(true)
                Log.i("도시", "도시도시")
            }
            else{//병원 이름인 경우
                startTask(false)
            }
            search_map.text = null
        }
        mapActivity_backbtn.setOnClickListener {
            finish()
        }

        refresh_button.setOnClickListener {
            arr.clear()
            initHospital()
        }
    }


    fun softkeyboardhide(){//입력창 focus되었을 때 나타나는 키보드 다시 내리는 함수
        val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        im.hideSoftInputFromWindow(search_map.windowToken, 0)
    }

    object BitmapUtils{//커스텀 마커이미지를 비트맵으로 변경
        fun resizeMapIcons(context: Context, iconId: Int, width: Int, height: Int):Bitmap{
            val imageBitmap = BitmapFactory.decodeResource(context.resources, iconId)
            return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
        }
    }

    fun startTask(cityCheck:Boolean){

        search_map_button.isClickable = true

        CoroutineScope(Dispatchers.Main).launch {
            CoroutineScope(Dispatchers.IO).launch{
                var argUrl = hospitalSearchUrl
                if(cityCheck){//도시명 검색한 경우
                    val searchEncode = URLEncoder.encode(searchKeyword, "utf-8")
                    argUrl = argUrl.plus("SIGUN_NM=").plus(searchEncode).plus("&pIndex=")
                }
                else{ //병원 이름 검색한 경우
                    argUrl = argUrl.plus("pIndex=")
                }
                arr.clear()
                for( pIndex in 1..30){

                    var curURL = argUrl.plus(pIndex.toString())
                    val url = URL(curURL)
                    val doc = Jsoup.connect(url.toString()).parser(Parser.xmlParser()).get()
                    var hospitals: Elements
                    hospitals = doc.select("row")
                    if(hospitals.size <= 0){
                        break;
                    }
                    Log.i("apicheck", hospitals.toString())
                    for (hospital in hospitals) {
                        val available = hospital.select("BSN_STATE_NM").text()

                        val latitude= hospital.select("REFINE_WGS84_LAT").text()
                        val longitude = hospital.select("REFINE_WGS84_LOGT").text()
                        val hosName = hospital.select("CENTER_NM").text()
                        val hosPhone = hospital.select("TELNO").text()
                        val hosAddress = hospital.select("REFINE_ROADNM_ADDR").text()
                        if(!cityCheck){//병원 검색한 경우
                            if(!hosName.contains(searchKeyword, true)){
                                continue
                            }
                        }
                        var l : LatLng
                        l = LatLng(latitude.toDouble(), longitude.toDouble())
                        Log.i("hospitalcheck", hospital.select("BIZPLC_NM").text().toString())
                        arr.add(HospitalInfo(hosName, hosPhone, l, hosAddress))
                        /*
                        try{
                            l = LatLng(latitude.toDouble(), longitude.toDouble())
                            Log.i("hospitalcheck", hospital.select("BIZPLC_NM").text().toString())
                            arr.add(HospitalInfo(hosName, hosPhone, l, hosAddress))
                        }catch ( e : NumberFormatException){
                            continue
                        }

                         */

                    }
                } //arr 배열에 정보 추가 완료

            }.join()
            //CSV search
            if(cityCheck){
                findPlaceCSV(searchKeyword)
            }else{
                findHospitalCSV(searchKeyword)
            }
            Log.i("hospital search num", arr.size.toString())

            googlemap.clear()


            for(hospital in arr){
                val position = hospital.hLatlng
                val options = MarkerOptions()
                options.position(position)
                options.icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
                var marker = googlemap.addMarker(options)
                marker?.tag = hospital
            }

        }

    }

    fun stopLocationUpdates(){
        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun getuserlocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //위치정보 동의 했는지 확인
            return
        }
        //서울역 37.5552002, 126.9706291
        //건대 공대 37.541223, 127.079283

        fusedLocationClient?.lastLocation?.addOnSuccessListener { location : Location? ->
            if (location != null){
                loc = LatLng(location.latitude, location.longitude)
                Log.i("현재위치", location.latitude.toString() +", "+location.longitude.toString())
            }
            else{//사용자의 기기에서 현재위치를 받을 수 없는 경우
                //loc = LatLng(37.5552002, 126.9706291)
                loc= LatLng(37.541223, 127.079283)
            }

        }
    }

    private fun startLocationUpdates() {//location정보 갱신

        locationRequest = com.google.android.gms.location.LocationRequest.create()?.apply{
            //interval = 10000
            //fastestInterval = 5000
            //priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            //priority = LocationRequest.QUALITY_HIGH_ACCURACY
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        Log.i("current", "success")
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                locationResult?:return
                for(location in locationResult.locations){
                    if(location != null){
                        loc = LatLng(location.latitude, location.longitude)
                    }

                    googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
                }
            }
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult?:return
//                for(location in locationResult.locations){
//                    loc = LatLng(location.latitude, location.longitude)
//                    googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f))
//                }
//            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest!!, locationCallback!!, Looper.getMainLooper())



    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                getuserlocation()
                startLocationUpdates()
                initMap()
            }
            else{
                Toast.makeText(this, "위치정보 제공에 동의해야 이용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{
            googlemap = it
            googlemap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10.0f))
            googlemap.setMinZoomPreference(10.0f)
            googlemap.setMaxZoomPreference(18.0f)
            val options = MarkerOptions()
            options.position(loc)
            options.icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
//            options.title("역")
//            options.snippet("서울역")
//            val mk1 = googlemap.addMarker(options)
//            mk1.showInfoWindow()
            initMapListener()
            readCSV() //광진구 병원 정보 추가
            initHospital()

        }
    }

    fun initMapListener(){
        googlemap.setOnMapClickListener {
            //googlemap.clear()//마커정보 전부 삭제
            map_linearlayout.visibility = View.GONE
        }
        googlemap.setOnMarkerClickListener(this)
    }

    private fun initHospital() {//api에서 병원 정보 가져오기
        /*
        val task = MyAsyncTask(object : MyAsyncTask.AsyncResponse{
            override fun readyToSetHospital(arr: ArrayList<HospitalInfo>) {

                //Log.i("hospitalnum", arr.size.toString())

                for(hospital in arr){
                    val position = hospital.hLatlng
                    val options = MarkerOptions()
                    options.position(position)
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    var marker = googlemap.addMarker(options)
                    marker?.tag = hospital
                }
            }

        })
        task.execute(hospitalURL)

         */

        CoroutineScope(Dispatchers.Main).launch {
            CoroutineScope(Dispatchers.IO).launch{
                for( pIndex in 1..30){
                    val argUrl = hospitalURL
                    var curURL = argUrl.plus(pIndex.toString())
                    val url = URL(curURL)
                    val doc = Jsoup.connect(url.toString()).parser(Parser.xmlParser()).get()

                    var hospitals: Elements
                    hospitals = doc.select("row")
                    if(hospitals.size <= 0){
                        break;
                    }
                    Log.i("apicheck", hospitals.toString())
                    for (hospital in hospitals) {
                        val available = hospital.select("BSN_STATE_NM").text()

                        Log.i("apicheck", "병원정보")
                        val latitude= hospital.select("REFINE_WGS84_LAT").text()
                        val longitude = hospital.select("REFINE_WGS84_LOGT").text()
                        val hosName = hospital.select("CENTER_NM").text()
                        val hosPhone = hospital.select("TELNO").text()
                        val hosAddress = hospital.select("REFINE_ROADNM_ADDR").text()
//                        if(latitude == null || longitude == null){
//                            continue
//                        }
                        var l : LatLng
                        l = LatLng(latitude.toDouble(), longitude.toDouble())
                        Log.i("hospitalcheck", hospital.select("BIZPLC_NM").text().toString())
                        arr.add(HospitalInfo(hosName, hosPhone, l, hosAddress))
                        /*
                        try{
                            l = LatLng(latitude.toDouble(), longitude.toDouble())
                            Log.i("hospitalcheck", hospital.select("BIZPLC_NM").text().toString())
                            arr.add(HospitalInfo(hosName, hosPhone, l, hosAddress))
                        }catch ( e : NumberFormatException){
                            continue
                        }

                         */

                    }
                } //arr 배열에 정보 추가 완료

            }.join()
            Log.i("hospitalnum", arr.size.toString())


            for(hospital in arr){
                val position = hospital.hLatlng
                val options = MarkerOptions()
                options.position(position)
                options.icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
                var marker = googlemap.addMarker(options)
                marker?.tag = hospital
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        map_linearlayout.visibility = View.VISIBLE
        val hosInfo = marker?.tag as HospitalInfo
        map_name.text = hosInfo.hName
        map_address.text = hosInfo.hAddress
        map_phone.text = hosInfo.hPhone

        return true
    }

    fun readCSV(){ //광진구 병원 정보
        var line:BufferedReader?=null
        var hospitalInfo=""
        try {
            line= BufferedReader(InputStreamReader(resources.openRawResource(R.raw.gwangjin_hospital),"UTF-8"))
            //첫 줄은 attribute name 이므로 건너뛴다.
//            hospitalInfo=line!!.readLine().toString()
//            Log.i("hospitalInfo first ",hospitalInfo)

            do{
                hospitalInfo=line!!.readLine().toString()
                Log.i("hospitalInfo first ",hospitalInfo)

                if(hospitalInfo.contains("NO")){
                    continue
                }else if(hospitalInfo.contains("끝")){
                    return
                }

                var infos=hospitalInfo.split(",")
                //infos[1]=병원이름, infos[3]=전화번호, infos[4]=경도, infos[5]=위도, infos[6]=주소
                Log.i("저장되는 정보 ",infos[1]+" "+infos[3]+" "+infos[4]+" "+infos[5]+" "+infos[6])
                var l : LatLng
                l = LatLng(infos[4].toDouble(), infos[5].toDouble())
                //Log.i("hospitalcheck", hospital.select("BIZPLC_NM").text().toString())
                var address=infos[6].replace("\"","")
                arr.add(HospitalInfo(infos[1], infos[3], l, address))

            }while(line!=null)

        }catch (e:IOException){
            Log.e("CSV Read Error : ",e.toString())
        }
    }

    fun findPlaceCSV(place:String){
        var line:BufferedReader?=null
        var hospitalInfo=""
        try {
            line= BufferedReader(InputStreamReader(resources.openRawResource(R.raw.gwangjin_hospital),"UTF-8"))
            //첫 줄은 attribute name 이므로 건너뛴다.
//            hospitalInfo=line!!.readLine().toString()
//            Log.i("hospitalInfo first ",hospitalInfo)

            do{

                hospitalInfo=line!!.readLine().toString()
                Log.i("hospitalInfo first ",hospitalInfo)

                if(hospitalInfo.contains("NO")){
                    continue
                }else if(hospitalInfo.contains("끝")){
                    return
                }

                var infos=hospitalInfo.split(",")
                //infos[1]=병원이름, infos[3]=전화번호, infos[4]=경도, infos[5]=위도, infos[6]=주소
                Log.i("저장되는 정보 ",infos[1]+" "+infos[3]+" "+infos[4]+" "+infos[5]+" "+infos[6])
                if(infos[6].contains(place)){
                    var l : LatLng
                    l = LatLng(infos[4].toDouble(), infos[5].toDouble())
                    var address=infos[6].replace("\"","")
                    arr.add(HospitalInfo(infos[1], infos[3], l, address))
                }
            }while(line!=null)

        }catch (e:IOException){
            Log.e("CSV Read Error : ",e.toString())
        }
    }

    fun findHospitalCSV(hospital:String){
        var line:BufferedReader?=null
        var hospitalInfo=""
        try {
            line= BufferedReader(InputStreamReader(resources.openRawResource(R.raw.gwangjin_hospital),"UTF-8"))
            //첫 줄은 attribute name 이므로 건너뛴다.
//            hospitalInfo=line!!.readLine().toString()
//            Log.i("hospitalInfo first ",hospitalInfo)

            do{

                hospitalInfo=line!!.readLine().toString()
                Log.i("hospitalInfo first ",hospitalInfo)

                if(hospitalInfo.contains("NO")){
                    continue
                }else if(hospitalInfo.contains("끝")){
                    return
                }

                var infos=hospitalInfo.split(",")
                //infos[1]=병원이름, infos[3]=전화번호, infos[4]=경도, infos[5]=위도, infos[6]=주소
                Log.i("저장되는 정보 ",infos[1]+" "+infos[3]+" "+infos[4]+" "+infos[5]+" "+infos[6])
                if(infos[1].contains(hospital)){
                    var l : LatLng
                    l = LatLng(infos[4].toDouble(), infos[5].toDouble())
                    var address=infos[6].replace("\"","")
                    arr.add(HospitalInfo(infos[1], infos[3], l, address))
                }
            }while(line!=null)

        }catch (e:IOException){
            Log.e("CSV Read Error : ",e.toString())
        }
    }

    /*
    private fun requestHospital(){
        val subscription = hospitalManager.getHospitalList()
            .subscribeOn(Schedulers.io())
            .subscribe(
                { retrievedMovie ->
                    (rv_movie)
                }
            )
    }
    class HospitalManager(){
        fun getHospitalList(): io.reactivex.Observable<List<HospitalInfo>> { //observable : 생산자
            return io.reactivex.Observable.create { subscriber ->
                val hospitalList = mutableListOf<HospitalInfo>()
                for( i in 1..10){

                }
                subscriber.onNext(hospitalList) // 구독자에게 데이터 발행 알림
                subscriber.onComplete() // 모든 데이터 발행 완료

            }
        }

    }

     */

}
