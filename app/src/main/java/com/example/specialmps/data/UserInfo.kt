package com.example.specialmps.data

data class UserInfo(var name:String, var id:String, var pw:String, var sex:String, var birth:String)
{
    constructor():this("noinfo","noinfo","noinfo","noinfo","noinfo")
}