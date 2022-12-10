package com.example.specialmps.data

data class Message(var message:String,var sender:String,var date: String, var time: String,var emotion:String) {
    constructor():this("no message","no sender","no date","no time","no emotion")
}