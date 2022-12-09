package com.example.specialmps

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tagmanager.Container
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList

class MenuActivity : AppCompatActivity() , DuoMenuView.OnMenuClickListener{

    lateinit var userid : String
    lateinit var name : String
    val mDatabase= FirebaseDatabase.getInstance()
    var result_ID:String=""
    lateinit var DateList:ArrayAdapter<String>
    var DateListString=ArrayList<String>()
    private var mViewHolder: ViewHolder?=null
    private var mMenuAdapter:MenuAdapter?=null
    private var mTitles=ArrayList<String>()
    var colorArray=ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }
        if(intent.hasExtra("name")){
            name = intent.getStringExtra("name").toString()
        }

        DateList=callDatelist()

        mTitles= ArrayList(Arrays.asList(*resources.getStringArray(R.array.menuOptions)))
        mViewHolder=ViewHolder()
        handleToolbar()
        handleMenu()
        handleDrawer()

        mMenuAdapter!!.setViewSelected(0,true)
        title=mTitles[0]

        findColor()
        init()

    }


    fun init(){

        //달력 생성
        val calendar=findViewById<MaterialCalendarView>(R.id.calender)
        calendar.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit()

        calendar.addDecorators(TodayDecorator(),SundayDecorator(),SaturdayDecorator())
        getDatesArraylist()

        //navigation drawer 방법
//        menu.setOnClickListener {
//            //메뉴 버튼 누르면 세부 메뉴 보여주기 --> 다른 곳을 눌렀을 때 화면 꺼지는 것도 구현
//            val drawerLayout=findViewById<DrawerLayout>(R.id.draw_layout)
//            drawerLayout.openDrawer(GravityCompat.START)
//
//            val id=findViewById<TextView>(R.id.userID)
//            id.setText(name+" 님")
//
//            val navi=findViewById<NavigationView>(R.id.navigation)
//            navi.setNavigationItemSelectedListener(object :NavigationView.OnNavigationItemSelectedListener{
//                override fun onNavigationItemSelected(item: MenuItem): Boolean {
//                    item.setChecked(true)
//                    drawerLayout.closeDrawers()
//                    val selectedMenu=item.itemId
//                    when(selectedMenu){
//                        R.id.record->{
//                            selectDay(DateList)
//                        }
//                        R.id.counseling->{
//                            showChattingPage()
//                        }
//                        R.id.hospital->{
//                            //병원검색 페이지로 이동
//                            showHospitalPage()
//                        }
//                        R.id.results ->{
//                            //최종결과만 있는 페이지 가기
//                            //showResultsPage()
//                        }
//                    }
//                    return true
//                }
//            })
//        }
//        val toolbar=findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_menu)
//        toolbar.setTitleTextColor(Color.parseColor("#d4a373"))
//        setSupportActionBar(toolbar)
//        supportActionBar!!.setDisplayShowTitleEnabled(false)

        newchat.setOnClickListener {
            showChattingPage()
        }
    }

    fun callDatelist(): ArrayAdapter<String> {

        val madapter=ArrayAdapter<String>(this,android.R.layout.select_dialog_singlechoice)

        //radio로 추가할 상담 날짜들 가져오기
        mDatabase.getReference("Record").child(userid).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                madapter.clear()

                if(snapshot.exists()){
                    for(date in snapshot.children){
                        val item=date.key.toString()
                        madapter.add(item)
                        //DateListString.add(item)
                        Log.i("each date",item)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return madapter
    }

    fun selectDay(items:ArrayAdapter<String>){

        var selectedItem=""
        val builder= AlertDialog.Builder(this).setTitle("상담 날짜 선택하기")
        builder.setSingleChoiceItems(items,-1, DialogInterface.OnClickListener{ dialog, which ->
            selectedItem= items.getItem(which).toString()
        }).setPositiveButton("완료",object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                result_ID=selectedItem
                showRecordPage()
            }
        }).setNegativeButton("취소", null).show()
    }

    fun showHospitalPage(){
        var i = Intent(this, HospitalActivity::class.java)
        startActivity(i)
    }

    fun showRecordPage(){
        var i = Intent(this, RecordedChat::class.java)
        i.putExtra("userID", userid)
        i.putExtra("resultID",result_ID)
        startActivity(i)
    }

    fun showChattingPage(){
        var i = Intent(this, ChattingActivity::class.java)
        i.putExtra("userID", userid)
        i.putExtra("name", name)
        startActivity(i)
    }

    fun getDatesArraylist(){ //상담했던 날짜 캘린더에 표시하기

        var dates=ArrayList<CalendarDay>()
        val calendar=findViewById<MaterialCalendarView>(R.id.calender)

        CoroutineScope(Dispatchers.IO).launch {
            mDatabase.getReference("Record").child(userid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(date in snapshot.children){
                            val item=date.key.toString()
                            DateListString.add(item)
                            //Log.i("날짜 리스트 개수 확인 ",DateListString.size.toString())
                            var date_list=item.split("-"," ")
                            var dot_year=date_list[0].toInt()
                            var dot_month=date_list[1].toInt()-1 //한 달씩 더해져서 나오므로 하나 빼줘야함.
                            var dot_day=date_list[2].toInt()

                            //달력에 표시할 날짜 가져오기
                            var date=Calendar.getInstance()
                            date.set(dot_year,dot_month,dot_day)
                            //Log.i("날짜 ",date_list[0]+date_list[1]+date_list[1])
                            //달력에 표시할 날짜 day list에 추가
                            var day=CalendarDay.from(date)
                            dates.add(day)
                            //Log.i("총 날짜 개수 ",dates.size.toString())
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        Handler(Looper.getMainLooper()).postDelayed({
            calender!!.removeDecorators()
            calender!!.invalidateDecorators()

            calendar.addDecorators(TodayDecorator(),SundayDecorator(),SaturdayDecorator())
            if(dates.size>0){
//                calendar!!.addDecorator(EventDecorator(dates,"#d4a373"))//색 지정하려면 인자 추가
                Log.i("dates size ",dates.size.toString())
                Log.i("color array size  ",colorArray.size.toString())
                for(i in dates.indices){
                    calender.addDecorator(EventDecorator(Collections.singleton(dates[i]),colorArray[i]))
                }
            }
        },1000)

    }

    private fun findColor(){
        var color="#d4a373"
        val carray= listOf("#FF8989", "#FFE088", "#9BAFEB", "#B0B0B0")
        for (i in 0..49){//db에 데이터 추가되면 숫자 변경
            var num=(0..3).random()
            colorArray.add(carray[num])
        }
        CoroutineScope(Dispatchers.IO).launch {
            mDatabase.getReference("Emotion").child(userid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (date in snapshot.children) {
                            val emotion=date.getValue(EmotionInfo::class.java)
                            if(emotion!=null){
                                Log.i("emotionColor ",emotion.emotionalColor)
                                colorArray.add(emotion.emotionalColor)
                            }else{
                                colorArray.add(color)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }
//        Log.i("color array size  ",colorArray.size.toString())

    }

    private fun handleToolbar(){
        mViewHolder!!.mToolbar.setTitleTextColor(Color.parseColor("#d4a373"))
        setSupportActionBar(mViewHolder!!.mToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun handleDrawer(){
        val DrawerToggle=DuoDrawerToggle(
            this, mViewHolder!!.mDrawerLayout,mViewHolder!!.mToolbar,
            R.string.navigation_drawer_open,R.string.navigation_drawer_close        )
        mViewHolder!!.mDrawerLayout.setDrawerListener(DrawerToggle)
        DrawerToggle.syncState()

    }

    private fun handleMenu(){
        mMenuAdapter= MenuAdapter(mTitles)
        mViewHolder!!.mDuoMenuView.setOnMenuClickListener(this)
        mViewHolder!!.mDuoMenuView.adapter=mMenuAdapter
    }

    private inner class ViewHolder internal constructor(){
        val mDrawerLayout:DuoDrawerLayout
        val mDuoMenuView:DuoMenuView
        val mToolbar:Toolbar

        init {
            val id=findViewById<TextView>(R.id.userID)
            id.setText(name+" 님")

            mDrawerLayout=findViewById<View>(R.id.draw_layout) as DuoDrawerLayout
            mDuoMenuView=mDrawerLayout.menuView as DuoMenuView
            mToolbar=findViewById<View>(R.id.toolbar_menu) as Toolbar
        }
    }

    override fun onFooterClicked() {
        //Toast.makeText(this,"Test clicked footer",Toast.LENGTH_SHORT).show()
    }

    override fun onHeaderClicked() {
        //Toast.makeText(this,"Test clicked header",Toast.LENGTH_SHORT).show()
    }

    override fun onOptionClicked(position: Int, objectClicked: Any?) {
        //set the toolbar title
        title=mTitles[position]

        //set the right options selected
        mMenuAdapter!!.setViewSelected(position,true)
        when(position){
            0->{showChattingPage()}
            1->{selectDay(DateList)}
            2->{showHospitalPage()}
        }
        //close the drawer
        mViewHolder!!.mDrawerLayout.closeDrawer()
    }
}

private class SaturdayDecorator:DayViewDecorator{
    val calendar=Calendar.getInstance()

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay=calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay==Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.BLUE))
    }
}

private class SundayDecorator:DayViewDecorator{
    val calendar=Calendar.getInstance()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        day?.copyTo(calendar)
        val weekDay=calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay==Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(Color.RED))
    }
}

private class TodayDecorator:DayViewDecorator{
    var date=CalendarDay.today()
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.equals(date)!!
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(StyleSpan(Typeface.BOLD))
        view?.addSpan(RelativeSizeSpan(1.2f))
        view?.addSpan(ForegroundColorSpan(Color.parseColor("#d4a373")))
    }
}

private class EventDecorator(dates:Collection<CalendarDay>?,color:String):DayViewDecorator{///////////
    val mDatabase= FirebaseDatabase.getInstance()
    val dates:HashSet<CalendarDay>
    var color=""
    var userid=""

    init {
        this.dates=HashSet(dates)
        this.color=color
    }

    override fun shouldDecorate(day: CalendarDay?): Boolean {
//        if(dates.contains(day)) {
//            //Log.i("dates.contains(day) ",day.toString())
//            //deco해야하는 날짜이면
//
//            color=findColor(day) //색 변경
//        }

        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        //#FF8989 빨, #FFE088 노, #9BAFEB 파, #B0B0B0 회 사용자 감정 색
        //#d4a373 기본 색
        Log.i("decorate : ",color) //색을 우선 다 뿌리고 날짜 지정해서 표시하는 듯,,,,,,,,,,,,,,,
        view?.addSpan(DotSpan(13F,Color.parseColor(color)))//사용자 감정 색
    }

    private fun findColor(day:CalendarDay?):String{
        var check=""
        if(day!!.day in 1..9){
            check=day!!.year.toString()+"-"+(day!!.month+1).toString()+"-0"+day!!.day.toString()
        }else{
            check=day!!.year.toString()+"-"+(day!!.month+1).toString()+"-"+day!!.day.toString()
        }
        //Log.i("day to string ",check)
        CoroutineScope(Dispatchers.IO).launch {
            mDatabase.getReference("Emotion").child(userid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        for(date in snapshot.children){
                            val item=date.key.toString()
//                            Log.i("date ",item)
                            if(item.contains(check)){ //상담날짜에 day가 해당되면 색 찾기
                                Log.i("item.contains(check) ",item)
                                val emotion=date.getValue(EmotionInfo::class.java)
                                if(emotion!=null){
                                    Log.i("emotion.emotionColor ",emotion.toString())
                                    color=emotion.emotionalColor
                                }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
       }
        return "#d4a373"
    }

}
