package com.example.specialmps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val hand=Handler()
        hand.postDelayed({
            val i=Intent(this,LoginActivity::class.java)
            startActivity(i)
            finish()
        },2000)

    }
}
