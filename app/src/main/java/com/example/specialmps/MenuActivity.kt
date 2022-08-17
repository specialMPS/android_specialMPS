package com.example.specialmps

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity() {
    lateinit var userid : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        if(intent.hasExtra("userID")){
            userid = intent.getStringExtra("userID").toString()
        }

        init()
    }

    fun init(){

        pastbtn.setOnClickListener {
            var i = Intent(this, RecordedChat::class.java)
            i.putExtra("userID", userid)
            startActivity(i)
        }
        consultbtn.setOnClickListener {
            var i = Intent(this, Chatting::class.java)
            i.putExtra("userID", userid)
            startActivity(i)
        }

        hospitalbtn.setOnClickListener {

        }
        mypagebtn.setOnClickListener {

        }

    }
}