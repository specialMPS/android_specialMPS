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
import android.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.specialmps.databinding.SlidingrootnavBinding
import com.google.android.gms.tagmanager.Container
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import kotlinx.android.synthetic.main.activity_menu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.*

class MenuActivity : AppCompatActivity() {

    lateinit var userid : String
    lateinit var name : String
    val mDatabase= FirebaseDatabase.getInstance()
    var result_ID:String=""
    lateinit var DateList:ArrayAdapter<String>
    var DateListString=ArrayList<String>()

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
        sliding_root_nav()
        init()
    }

    fun init(){

        //달력 생성
        val calendar=findViewById<MaterialCalendarView>(R.id.calender)
        calendar.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit()

        calendar.addDecorators(TodayDecorator(),SundayDecorator(),SaturdayDecorator())
        getDatesArraylist()

//        menu.setOnClickListener {
//            //메뉴 버튼 누르면 세부 메뉴 보여주기 --> 다른 곳을 눌렀을 때 화면 꺼지는 것도 구현
//
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

        val toolbar=findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_menu)
        toolbar.setTitleTextColor(Color.parseColor("#d4a373"))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        SlidingRootNavBuilder(this).withToolbarMenuToggle(toolbar).withMenuLayout(R.layout.slidingrootnav)
            .withContentClickableWhenMenuOpened(true).inject()

        newchat.setOnClickListener {
            showChattingPage()
        }
    }

    fun sliding_root_nav(){
        //slidingrootnav 레이아웃에서 클릭 이벤트
        val inflater=layoutInflater.inflate(R.layout.slidingrootnav,null)
        val id=inflater.findViewById<TextView>(R.id.userID)
        id.setText(name+" 님")


        val counseling=inflater.findViewById<TextView>(R.id.counseling)
        counseling.setOnClickListener{
            showChattingPage()
        }

        val record=inflater.findViewById<TextView>(R.id.record)
        record.setOnClickListener {
            selectDay(DateList)
        }

        val hospital=inflater.findViewById<TextView>(R.id.hospital)
        hospital.setOnClickListener {
            showHospitalPage()
        }

        val results=inflater.findViewById<TextView>(R.id.results)
        results.setOnClickListener {
            //최종결과만 있는 페이지 가기
            //showResultsPage()
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
                calendar!!.addDecorator(EventDecorator(dates))//색 지정하려면 인자 추가
            }

        },1000)

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

private class EventDecorator(dates:Collection<CalendarDay>?):DayViewDecorator{///////////
    val dates:HashSet<CalendarDay>

    init {
        this.dates=HashSet(dates)
    }

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(8F,Color.parseColor("#d4a373")))//기본 색
    }


}
