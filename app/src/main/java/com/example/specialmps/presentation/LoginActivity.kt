package com.example.specialmps.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.specialmps.R
import com.example.specialmps.data.UserInfo
import com.example.specialmps.presentation.signup.SignUpActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    val mDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        btnClick()
    }

    fun checkAccount(id: String, pw: String) {
        val database = mDatabase.getReference("User")
        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                for (shot in snapshot.children) {
                    if (shot.key.toString() == id) {
                        Log.i("id일치", id)
                        val user = shot.getValue(UserInfo::class.java)
                        if (user != null) {
                            if (user.pw == pw) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    user.name + "님이 로그인하셨습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val i = Intent(this@LoginActivity, MenuActivity::class.java)
                                Log.i("메뉴 액티비티 넘어가기 직전", " 성공")
                                i.putExtra("userID", id)
                                i.putExtra("name", user.name)
                                //i.putExtra("resultID","2022-07-17")
                                startActivity(i)
                                finish()
                                // */
                            }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "없는 계정이거나 비밀번호가 맞지 않습니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database Connection Error", error.message)

            }
        })
    }

    fun btnClick() {
        signup_btn.setOnClickListener {
            //회원가입 창
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }

        login_btn.setOnClickListener {
            val _id = id_input.text.toString()
            val _pw = pwd_input.text.toString()

            checkAccount(_id, _pw)
            val hand = Handler()
            hand.postDelayed({
                id_input.setText("")
                pwd_input.setText("")
            }, 2000)
        }
    }
}
