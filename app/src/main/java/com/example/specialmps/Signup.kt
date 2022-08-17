package com.example.specialmps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
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

        signup_register.setOnClickListener {
            var name = signup_name.text.toString()
            var id = signup_id.text.toString()
            var pw = signup_pw.text.toString()
            //성별, 생년월일
            var date = signup_date.text.toString()
            if(id == "" ||pw == "" || name == "" || date == ""){
                Toast.makeText(this, "모든 정보를 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(date.toInt() < 1900 || date.toInt() > 2022){
                Toast.makeText(this, "태어난 연도를 다시 입력해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var sex = ""
            if(signup_girl.isChecked){
                sex = "female"
            }
            if(signup_boy.isChecked){
                sex = "male"
            }
            val userinfo = UserInfo(name, id, pw, sex, date)
            //아이디 중복체크 후 데이터베이스에 추가
            var rootref = FirebaseDatabase.getInstance().getReference()
            var checkID = rootref.child("User").child(id)

            checkID.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(datasnapshot: DataSnapshot) {
                    if(datasnapshot.exists()){//아이디가 이미 존재
                        Toast.makeText(this@Signup, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        this@Signup.register(userinfo, id)
                    }
                }

            })

        }
        signup_cancel.setOnClickListener {
            var i = Intent()
            i.putExtra("success", false)
            setResult(Activity.RESULT_CANCELED, i)
            finish()
        }
    }


    fun register(userinfo : UserInfo, uid : String){
        rdatabase.child(uid).setValue(userinfo)
        var i = Intent()
        i.putExtra("success", true)
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}