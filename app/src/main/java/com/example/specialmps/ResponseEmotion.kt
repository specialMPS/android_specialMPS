package com.example.specialmps

import kotlinx.serialization.Serializable

@Serializable
data class ResponseEmotion(
    val neutral: Float = 0f,
    val happy: Float = 0f,
    val surprise: Float = 0f,
    val tense: Float = 0f,
    val pain: Float = 0f,
    val anger: Float = 0f,
    val miserable: Float = 0f,
    val depress: Float = 0f,
    val tired: Float = 0f
)
