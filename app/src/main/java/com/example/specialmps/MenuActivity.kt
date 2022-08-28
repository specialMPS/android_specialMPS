package com.example.specialmps

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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
import kotlinx.android.synthetic.main.activity_menu.*
import java.util.*

class MenuActivity : AppCompatActivity() {

    lateinit var userid : String
    val mDatabase= FirebaseDatabase.getInstance()
    var result_ID:String=""
    lateinit var DateList:ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }

        DateList=callDatelist()
        //Log.i("DateList",DateList[0])
        init()
    }

    fun init(){

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

                        }
                    }
                    return true
                }
            })
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
}