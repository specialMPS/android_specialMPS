package com.example.specialmps

import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class Message(var message:String,var sender:String,var date: String, var time: String,var emotion:String) {
    constructor():this("no message","no sender","no date","no time","no emotion")
}