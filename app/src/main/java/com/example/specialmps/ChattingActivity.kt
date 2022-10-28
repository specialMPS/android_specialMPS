package com.example.specialmps

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_chatting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ChattingActivity : AppCompatActivity() {

    val first_topic = getString(R.string.chat_start)
    lateinit var mMessageAdapter: MessageListAdapter
    val messageList = mutableListOf<Message>()
    var topic_list = ArrayList<String>()
    var user: String = ""
    val mDatabase = FirebaseDatabase.getInstance()
    var chat_start_time: String = ""
    val serverURL = "http://172.20.10.12:8080/chat?s="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        //뒤로가기 버튼
        val toolbar: Toolbar = findViewById(R.id.toolbar_channel)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true) //마우스 왼쪽 버튼 사용
        supportActionBar!!.setDisplayShowTitleEnabled(false) //toolbar에 title 보이지 않도록 설정

        if (intent.hasExtra("userID")) {
            user = intent.getStringExtra("userID").toString()
            Log.i("userid", user)
        }

        init_chat_setting()
        init_UI_event()


    }

    fun init_UI_event() {
        var mMessageRecycler: RecyclerView = findViewById(R.id.recycler_chat)
        mMessageRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mMessageAdapter = MessageListAdapter(this, messageList)
        mMessageRecycler.adapter = mMessageAdapter

        //var table=mDatabase.getReference("Record").child(user).child(chat_start_time)

        //사용자가 메세지를 입력했을 때
        btn_chat_send.setOnClickListener {
            val chat_data = Message(
                edit_chat_message.text.toString(),
                "user",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
                currentTime(),
                "no emotion" ///////////////////////////////////////사용자 챗 감정 추가
            )
            //list에 추가
            messageList.add(chat_data)
            getAIresponse(edit_chat_message.text.toString())
            edit_chat_message.setText("")
            mMessageAdapter.notifyItemInserted(mMessageAdapter.itemCount)
            mMessageRecycler.scrollToPosition(mMessageAdapter.itemCount - 1)
/*
            var table=mDatabase.getReference("Record").child(user).child(currentDate)
            table.push().setValue(chat_data).addOnSuccessListener {
                edit_chat_message.setText("")
                messageList.add(chat_data)
                mMessageAdapter.notifyItemInserted(mMessageAdapter.itemCount)
            }
*/
        }
    }

    fun init_chat_setting() {
        //주제 데이터 읽어오기
        val scan = Scanner(resources.openRawResource(R.raw.q_list))
        while (scan.hasNextLine()) {
            topic_list.add(scan.nextLine())
        }
        scan.close()
        //현재날짜(DB)
        var currentDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)

        //Log.i("date+time",currentDate+" "+currentTime())

        //chatting 날짜 및 시작 시간에 따라 table 구분 짓기
        chat_start_time = currentDate + " " + currentTime()

        val first = Message(first_topic, "chatbot", currentDate, currentTime(),"no emotion")
        //질문 리스트 뽑기
        val rand = Random().nextInt(topic_list.size)
        val second_topic = topic_list[rand]
        val second = Message(second_topic, "chatbot", currentDate, currentTime(),"no emotion")

        messageList.add(first)
        messageList.add(second)
    }

    fun getAIresponse(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            CoroutineScope(Dispatchers.IO).launch {
                var curURL = serverURL.plus(message)
                Log.i("chatting", curURL)
                val url = URL(curURL)
                val doc = Jsoup.connect(url.toString()).get()
                Log.i("doc", doc.toString())

                val elements: Elements = doc.select("body")
                Log.i("elements", elements.toString())

                for (element in elements) {
                    val jsons = JSONObject(element.text())

                    Log.i("test2", jsons.getString("answer"))
                    addChatbotDB(jsons.getString("answer"))
                }

            }.join()
            mMessageAdapter.notifyItemInserted(mMessageAdapter.itemCount)
        }
    }

    fun addChatbotDB(chat: String) {
        val chat_data = Message(
            chat,
            "chatbot",
            LocalDateTime.now().format(DateTimeFormatter.ISO_DATE),
            currentTime(),
            "no emotion"
        )
        //list에 추가
        messageList.add(chat_data)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //toolbar의 back이 눌렸을 때
                val builder = AlertDialog.Builder(this)
                builder.setMessage("결과를 보지 않고 대화를 종료하시겠습니까?\n결과는 저장되지 않습니다.")
                    .setPositiveButton("확인", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            finish() //현재 액티비티 종료
                        }
                    }).setNegativeButton("취소", null).show()
                //finish() //현재 액티비티 종료
                return true
            }
            R.id.toolbar_exit -> {
                //Log.i("toolbar exit btn"," click")
                //종료 버튼을 눌렀을 때 다이얼로그 생성
                val builder = AlertDialog.Builder(this)
                builder.setMessage("대화를 종료하시겠습니까?")
                    .setPositiveButton("결과보기", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            //대화 리스트 데이터베이스에 저장하고 검사결과 페이지로 넘어가기
                            saveDB(messageList)
                        }
                    }).setNegativeButton("취소", null).show()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chatting_tool, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun currentTime(): String {//현재시간
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        return time
    }

    fun saveDB(chat: List<Message>) { //대화종료 후
        Log.i("saveDB ", chat.size.toString())
        //대화종료 후 messageList를 firebase에 추가
        var table = mDatabase.getReference("Record").child(user).child(chat_start_time)
        var check = 0

        for (i in chat) { //message 객체 삽입
            table.push().setValue(i).addOnSuccessListener {
                check++
                Log.i("디비 저장 개수 체크 ", check.toString())
            }
        }

        var i = Intent(this, ResultActivity::class.java)
        i.putExtra("userID", user)
        startActivity(i)

    }


}