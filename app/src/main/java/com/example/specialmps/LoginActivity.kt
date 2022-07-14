package com.example.specialmps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    val mDatabase=FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnClick()
    }

    fun checkAccount(id:String, pw:String){
        val database=mDatabase.getReference("Users")
        database.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for(shot in snapshot.children){
                    if(shot.key.toString()==id){
                        val user=shot.getValue(UserInfo::class.java)
                        if(user!=null){
                            if(user.pw==pw){
                                Toast.makeText(this@LoginActivity,user.name+"님이 로그인하셨습니다.",Toast.LENGTH_SHORT).show()
                                val i=Intent(this@LoginActivity,MainActivity::class.java)
                                i.putExtra("userID",id)
                                startActivity(i)
                                finish()
                            }
                        }else{
                            Toast.makeText(this@LoginActivity,"없는 계정이거나 비밀번호가 맞지 않습니다.",Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this@LoginActivity,"없는 계정이거나 비밀번호가 맞지 않습니다.",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database Connection Error",error.message)

            }
        })
    }

    fun btnClick(){
        signup.setOnClickListener {
            //회원가입 창
            val i=Intent(this,Signup::class.java)
            startActivity(i)
            finish()
        }

        login.setOnClickListener {
            val _id=id.text.toString()
            val _pw=password.text.toString()

            checkAccount(_id,_pw)
            val hand=Handler()
            hand.postDelayed({
                id.setText("")
                password.setText("")
            },2000)
        }
    }
}
