package com.example.specialmps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        //뒤로가기 버튼
        val toolbar:Toolbar=findViewById(R.id.toolbar_channel)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false) //toolbar에 title 보이지 않도록 설정

        if(intent.hasExtra("userID")){
            user= intent.getStringExtra("userID").toString()
            Log.i("userid",user)
        }

        //주제 데이터 읽어오기
        val scan=Scanner(resources.openRawResource(R.raw.q_list))
        while(scan.hasNextLine()){
            topic_list.add(scan.nextLine())
        }
        scan.close()

        //현재날짜
        val currentDate=LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)

        Log.i("date+time",currentDate+" "+currentTime())

        val first=Message(first_topic,"chatbot",currentDate,currentTime())
        //질문 리스트 뽑기
        val rand=Random().nextInt(topic_list.size)
        val second_topic=topic_list[rand]
        val second=Message(second_topic,"chatbot",currentDate,currentTime())

        val table=mDatabase.getReference("Record").child(user).child(currentDate) //진단결과 id key 자동생성

        //챗봇 처음 대화시작 ui 생성 및 데이터베이스에 추가
        table.push().setValue(first)
        table.push().setValue(second)

        messageList.add(first)
        messageList.add(second)

        var mMessageRecycler: RecyclerView =findViewById(R.id.recycler_chat)
        mMessageRecycler.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        mMessageAdapter=MessageListAdapter(this,messageList)
        mMessageRecycler.adapter=mMessageAdapter

        //사용자가 메세지를 입력했을 때
        btn_chat_send.setOnClickListener {
            val chat_data=Message(edit_chat_message.text.toString(),"user",currentDate,currentTime())
            //db에 추가
            table.push().setValue(chat_data).addOnSuccessListener {
                edit_chat_message.setText("")
                messageList.add(chat_data)
                mMessageAdapter.notifyItemInserted(mMessageAdapter.itemCount)
            }

        }

        //챗봇 대답 UI 생성 및 데이터베이스에 추가
        
        
        //대화종료 후 messageList를 firebase에 추가-->보류
        //saveDB(messageList)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.home -> { //toolbar의 back이 눌렸을 때
//                this.finish() //현재 액티비티 종료
//                return true
//            }
//
//        }
//        return super.onOptionsItemSelected(item)
//    }

    fun currentTime():String{//현재시간
        val time=LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        return time
    }

    fun saveDB(chat:List<Message>){ //대화종료 후
        //대화종료 후 messageList를 firebase에 추가
        val mdatabase=FirebaseDatabase.getInstance()
        var table=mdatabase.getReference("Record")
        var userid=table.child(user)
        //진단 결과 id(자동생성 key)
        var resultid=userid.push()
        for(i in chat){ //message 객체 삽입
            resultid.child("chat message").setValue(i)
        }
    }
}