package com.example.specialmps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    }

    fun checkID(id:String){
        val database=mDatabase.getReference("Users")
        database.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                for(shot in snapshot.children){
                    if(shot.key.toString()==id){
                        
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database Connection Error",error.message)

            }
        })
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
