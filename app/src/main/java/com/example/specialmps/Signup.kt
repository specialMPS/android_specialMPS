package com.example.specialmps

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_signup.*

class Signup : AppCompatActivity() {
    lateinit var rdatabase : DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        init()
    }
    private fun init(){
        rdatabase = FirebaseDatabase.getInstance().getReference()
        checkIDbutton.setOnClickListener {
            var id = signup_id.text.toString()

        }
        signUpbutton.setOnClickListener {
            var name = signup_name.text.toString()
            var id = signup_id.text.toString()
            var pw = signup_pw.text.toString()
            //성별, 생년월일

            if(id == "" ||pw == "" || name == ""){
                Toast.makeText(this, "모든 정보를 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }
    }

}