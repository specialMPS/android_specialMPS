package com.example.specialmps.presentation.signup

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpViewModel : ViewModel() {
    var rdatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun isEmpty(id: String, pw: String, name: String, date: String): Boolean {
        return id == "" || pw == "" || name == "" || date == ""
    }

}