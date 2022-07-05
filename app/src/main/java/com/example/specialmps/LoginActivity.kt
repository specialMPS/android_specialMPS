package com.example.specialmps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    //val mDatabase=FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun checkID(id:String){
        //val database=mDatabase.get
    }

    fun checkPW(){

    }

    fun btnClick(){
        signup.setOnClickListener {

        }

        login.setOnClickListener {
            val id=id.text.toString()
            val pw=password.text.toString()

            checkID(id)
        }
    }
}
