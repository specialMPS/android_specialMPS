package com.example.specialmps

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kotlinx.android.synthetic.main.activity_menu.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class MenuActivity : AppCompatActivity() {

    lateinit var userid : String
    val mDatabase= FirebaseDatabase.getInstance()
    var result_ID:String=""
    lateinit var DateList:ArrayAdapter<String>
    lateinit var CalendarList:HashSet<CalendarDay>
    var DateListString=ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }

        DateList=callDatelist()
        //Log.i("DateList",DateList[0])
        CalendarList=getDatesArraylist()
        init()
    }

    fun init(){

        //달력 생성
        val calendar=findViewById<MaterialCalendarView>(R.id.calender)
        calendar.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit()

        calendar.addDecorators(TodayDecorator(),SundayDecorator(),SaturdayDecorator())
        calendar.addDecorator(EventDecorator(CalendarList))

        menu.setOnClickListener {
            //메뉴 버튼 누르면 세부 메뉴 보여주기 --> 다른 곳을 눌렀을 때 화면 꺼지는 것도 구현

            val drawerLayout=findViewById<DrawerLayout>(R.id.draw_layout)
            drawerLayout.openDrawer(GravityCompat.START)

            val id=findViewById<TextView>(R.id.userID)
            id.setText(userid+" 님")

            val navi=findViewById<NavigationView>(R.id.navigation)
            navi.setNavigationItemSelectedListener(object :NavigationView.OnNavigationItemSelectedListener{
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    item.setChecked(true)
                    drawerLayout.closeDrawers()
                    val selectedMenu=item.itemId
                    when(selectedMenu){
                        R.id.record->{
                            selectDay(DateList)
                        }
                        R.id.counseling->{
                            showChattingPage()
                        }
                        R.id.hospital->{
                            //병원검색 페이지로 이동
                            showHospitalPage()
                        }
                    }
                    return true
                }
            })
        }

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
        var i = Intent(this, Chatting::class.java)
        i.putExtra("userID", userid)
        startActivity(i)
    }

    fun getDatesArraylist():HashSet<CalendarDay>{

        var dateslist=HashSet<CalendarDay>()
        mDatabase.getReference("Record").child(userid).addValueEventListener(object : //Result 테이블로 수정
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(date in snapshot.children){
                        val item=date.key.toString()
                        val datetime=item.split(" ")
                        Log.i("datetime[0]",datetime[0])
                        dateslist.add(dateTocalendar(datetime[0]))
                        DateListString.add(item)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return dateslist
    }

    fun dateTocalendar(date:String):CalendarDay{

        val cal=Calendar.getInstance()
        val format=SimpleDateFormat("yyyy-MM-dd")
        val day=format.parse(date)
        day.month=day.month+1
        cal.set(day.year,day.month,day.day)
        val resultdate=CalendarDay.from(day)

        Log.i("캘린더형식으로 변경",resultdate.toString())
        return resultdate
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

private class EventDecorator(dates:Collection<CalendarDay>?):DayViewDecorator{//////////////////////////////////////////////////////
    val dates:HashSet<CalendarDay>

    init {
        this.dates=HashSet(dates)
    }

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(DotSpan(5F,Color.parseColor("#d4a373")))
        //view?.addSpan(DotSpan(3F,Color.BLUE))
    }
}