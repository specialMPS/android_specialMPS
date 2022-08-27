package com.example.specialmps

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chatting.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Chatting : AppCompatActivity() {

    val first_topic="안녕하세요. 오늘은 어떤 기분으로 저를 찾아오셨나요?"
    lateinit var mMessageAdapter:MessageListAdapter
    val messageList= mutableListOf<Message>()
    var topic_list=ArrayList<String>()
    var user:String=""
    val mDatabase=FirebaseDatabase.getInstance()
    var chat_start_time:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        //뒤로가기 버튼
        val toolbar:Toolbar=findViewById(R.id.toolbar_channel)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) //마우스 왼쪽 버튼 사용
        supportActionBar!!.setDisplayShowTitleEnabled(false) //toolbar에 title 보이지 않도록 설정

        if(intent.hasExtra("userID")){
            user= intent.getStringExtra("userID").toString()
            Log.i("userid",user)
        }
//        if(intent.hasExtra("chat_not_exit_date")){
//            chat_date=intent.getStringExtra("chat_not_exit_date").toString()
//            Log.i("chat_not_exit_date",chat_date)
//        }
        
//        if(chat_date!=""){ //결과를 보지않고 종료된 가장 최근 채팅이 있다면 무조건 채팅 이어서 하게끔
//            call_previous_chat()
//            val builder=AlertDialog.Builder(this)
//            builder.setMessage("결과를 보지 않고 종료된 채팅이 있습니다.\n이어서 하시겠습니까?")
//                .setPositiveButton("예",object :DialogInterface.OnClickListener{
//                    override fun onClick(p0: DialogInterface?, p1: Int) {
//                        //최근 채팅 데이터 불러오기
//                        call_previous_chat()
//                    }
//                }).setNegativeButton("아니오",object :DialogInterface.OnClickListener{
//                    override fun onClick(p0: DialogInterface?, p1: Int) {
//                        new_chat_start(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
//                    }
//                }).show()
//        }

        //주제 데이터 읽어오기
        val scan=Scanner(resources.openRawResource(R.raw.q_list))
        while(scan.hasNextLine()){
            topic_list.add(scan.nextLine())
        }
        scan.close()

        //현재날짜(DB)
        var currentDate=LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)

        //Log.i("date+time",currentDate+" "+currentTime())


        //chatting 날짜 및 시작 시간에 따라 table 구분 짓기
        chat_start_time=currentTime()

        val first=Message(first_topic,"chatbot",currentDate,currentTime())
        //질문 리스트 뽑기
        val rand=Random().nextInt(topic_list.size)
        val second_topic=topic_list[rand]
        val second=Message(second_topic,"chatbot",currentDate,currentTime())

        messageList.add(first)
        messageList.add(second)

        var mMessageRecycler: RecyclerView =findViewById(R.id.recycler_chat)
        mMessageRecycler.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        mMessageAdapter=MessageListAdapter(this,messageList)
        mMessageRecycler.adapter=mMessageAdapter

        //사용자가 메세지를 입력했을 때
        btn_chat_send.setOnClickListener {
            val chat_data=Message(edit_chat_message.text.toString(),"user",LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),currentTime())
            //list에 추가
            messageList.add(chat_data)
            edit_chat_message.setText("")
            mMessageAdapter.notifyItemInserted(mMessageAdapter.itemCount)
//            table.push().setValue(chat_data).addOnSuccessListener {
//                edit_chat_message.setText("")
//                messageList.add(chat_data)
//                mMessageAdapter.notifyItemInserted(mMessageAdapter.itemCount)
//            }

        }

        //챗봇 대답 UI 생성 및 리스트에 추가
        
        
        //대화종료 후 messageList를 firebase에 추가-->보류(나가기 버튼 누르면 수행)
        //saveDB(messageList)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> { //toolbar의 back이 눌렸을 때
                val builder=AlertDialog.Builder(this)
                builder.setMessage("결과를 보지 않고 대화를 종료하시겠습니까?\n결과는 저장되지 않습니다.")
                    .setPositiveButton("확인",object :DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            finish() //현재 액티비티 종료
                        }
                    }).setNegativeButton("취소",null).show()
                finish() //현재 액티비티 종료
                return true
            }
            R.id.toolbar_exit -> {
                //Log.i("toolbar exit btn"," click")
                //종료 버튼을 눌렀을 때 다이얼로그 생성
                val builder=AlertDialog.Builder(this)
                builder.setMessage("대화를 종료하시겠습니까?")
                    .setPositiveButton("결과보기",object :DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            //대화 리스트 데이터베이스에 저장
                            saveDB(messageList)
                            //검사결과 페이지로 넘어가기

                            //검사결과 페이지로 넘어가면서 결과 table에 상담 날짜로 저장
                        }
                    }).setNegativeButton("취소",null).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chatting_tool,menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun currentTime():String{//현재시간
        val time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        return time
    }

    fun saveDB(chat:List<Message>){ //대화종료 후
        //대화종료 후 messageList를 firebase에 추가
        var table=mDatabase.getReference("Record")
        var userid=table.child(user+" "+chat_start_time)
        //진단 결과 id(자동생성 key)
        for(i in chat){ //message 객체 삽입
            userid.push().setValue(i)
        }
    }


}