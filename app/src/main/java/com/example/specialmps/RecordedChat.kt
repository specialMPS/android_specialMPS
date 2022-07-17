package com.example.specialmps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class RecordedChat : AppCompatActivity() {

    lateinit var mMessageAdapter:MessageListAdapter
    lateinit var rdatabase:FirebaseDatabase
    lateinit var messageList: MutableList<Message>
    var user:String=""
    var result_ID:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recorded_chat)

        if(intent.hasExtra("userID")){
            user= intent.getStringExtra("userID").toString()
        }
        if(intent.hasExtra("result_id")){
            result_ID=intent.getStringExtra("result_id").toString()
        }

        //데이터베이스에서 대화목록 가져오기-Message List 생성
        rdatabase.getReference(user).child(result_ID).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(snap in snapshot.children){
                        val chat_data=snap.getValue(Message::class.java)
                        if (chat_data != null) {
                            messageList.add(chat_data)
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
    }

}