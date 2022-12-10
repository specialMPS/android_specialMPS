package com.example.specialmps.data

import java.io.Serializable


data class EmotionInfo(
    val neutral: Float = 0f,
    val happy: Float = 0f,
    val surprise: Float = 0f,
    val tense: Float = 0f,
    val pain: Float = 0f,
    val anger: Float = 0f,
    val miserable: Float = 0f,
    val depress: Float = 0f,
    val tired: Float = 0f,
    var emotionalColor:String="noinfo"
):Serializable
