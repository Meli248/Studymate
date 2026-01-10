package com.example.study.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val profileImage: String = "",
    val course: String = "",
    val semester: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "fullName" to fullName,
            "email" to email,
            "profileImage" to profileImage,
            "course" to course,
            "semester" to semester
        )
    }
}