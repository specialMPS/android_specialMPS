package com.example.specialmps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class RecordedChat : AppCompatActivity() {

    lateinit var mMessageAdapter:MessageListAdapter
    var messageList= mutableListOf<Message>()
    val mDatabase=FirebaseDatabase.getInstance()
    var user:String=""
    var result_ID:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorded_chat)

        //toolbar 뒤로가기 생성
        val toolbar: Toolbar =findViewById(R.id.toolbar_channel)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false) //toolbar에 title 보이지 않도록 설정

        if(intent.hasExtra("userID")){
            user= intent.getStringExtra("userID").toString()
        }
        if(intent.hasExtra("resultID")){
            result_ID=intent.getStringExtra("resultID").toString()
        }

        //데이터베이스에서 대화목록 가져오기-Message List 생성
        mDatabase.getReference("Record").child(user).child(result_ID).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        val chat_data=snap.getValue(Message::class.java)
                        if (chat_data != null) {
                            messageList.add(chat_data)
                            Log.i("chat content",chat_data.message)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })

        var mMessageRecycler: RecyclerView =findViewById(R.id.record_recycler_chat)
        mMessageRecycler.layoutManager= LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        mMessageAdapter=MessageListAdapter(this,messageList)
        mMessageRecycler.adapter=mMessageAdapter
    }

}