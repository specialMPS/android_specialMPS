package com.example.specialmps.presentation.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.specialmps.R
import com.example.specialmps.data.UserInfo
import com.example.specialmps.databinding.ActivitySignupBinding
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity : AppCompatActivity() {
    lateinit var rdatabase: DatabaseReference

    private lateinit var binding: ActivitySignupBinding
    private val viewModel by viewModels<SignUpViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }

    private fun initListener() {
        rdatabase = FirebaseDatabase.getInstance().getReference()
        binding.signupRegister.setOnClickListener {
            var name = binding.signupName.text.toString()
            var id = binding.signupId.text.toString()
            var pw = binding.signupPw.text.toString()
            //성별, 생년월일
            var date = binding.signupDate.text.toString()
            if (viewModel.isEmpty(id, pw, name, date)) {
                Toast.makeText(this, R.string.signup_info_request, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (date.toInt() < 1900 || date.toInt() > 2022) {
                Toast.makeText(this, R.string.signup_birth_request, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var sex = ""
            if (binding.signupGirl.isChecked) {
                sex = "female"
            }
            if (binding.signupBoy.isChecked) {
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
                    if (datasnapshot.exists()) {//아이디가 이미 존재
                        Toast.makeText(
                            this@SignUpActivity,
                            R.string.signup_id_exist,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        this@SignUpActivity.register(userinfo, id)
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


    private fun register(userinfo: UserInfo, uid: String) {
        rdatabase.child("User").child(uid).setValue(userinfo)
        var i = Intent()
        i.putExtra("success", true)
        setResult(Activity.RESULT_OK, i)
        finish()
    }
}