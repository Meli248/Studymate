package com.example.study.model

data class User(
    val userId:String="",
    val email: String="",
    val password:String="",
    val fullName: String


    ){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "email" to email,
            "password" to password,
            "fullName" to fullName,

        )
    }

}
