package com.example.study.model

data class Subject(
    val subjectId: String = "",
    val userId: String = "",
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "subjectId" to subjectId,
            "userId" to userId,
            "name" to name,
            "createdAt" to createdAt
        )
    }
}