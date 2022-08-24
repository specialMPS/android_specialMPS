package com.example.specialmps

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    var chatting:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }

        DateList=callDatelist()
        //Log.i("DateList",DateList[0])
        callresultlist()
        init()
    }

    fun init(){

        pastbtn.setOnClickListener {
            selectDay(DateList)
        }
        consultbtn.setOnClickListener {
            var i = Intent(this, Chatting::class.java)
            i.putExtra("userID", userid)
            if(chatting!=""){
                i.putExtra("chat_not_exit_date",chatting)
                Log.i("chat_not_exit_date",chatting)
            }
            startActivity(i)
        }

        hospitalbtn.setOnClickListener {

        }
        mypagebtn.setOnClickListener {

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

    fun callresultlist(){
        //결과테이블의 개수와 채팅 테이블의 개수가 맞지 않을 때(개수 차이는 무조건 1이라고 판단)
        //채팅 테이블의 마지막 채팅 날짜로 연이어 채팅 하게끔 한다.
        mDatabase.getReference("Result").child(userid).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    if(snapshot.childrenCount.toInt()!=DateList.count){
                        chatting=
                            DateList.getItem(DateList.count-1).toString() //결과 추가 안된 채팅 날짜 설정
                        Log.i("resultid추가",result_ID)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun selectDay(items:ArrayAdapter<String>){

        var selectedItem=""
        val builder= AlertDialog.Builder(this).setTitle("상담 날짜 선택하기")
        builder.setSingleChoiceItems(items,-1, DialogInterface.OnClickListener{ dialog, which ->
            selectedItem= items.getItem(which).toString()
        }).setPositiveButton("완료",object : DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                result_ID=selectedItem
                showPage()
            }
        }).setNegativeButton("취소", null).show()
    }

    fun showPage(){
        var i = Intent(this, RecordedChat::class.java)
        i.putExtra("userID", userid)
        i.putExtra("resultID",result_ID)
        startActivity(i)
    }
}