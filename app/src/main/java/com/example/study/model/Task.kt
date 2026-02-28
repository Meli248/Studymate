package com.example.study.model

import com.google.firebase.database.PropertyName

data class Task(
    val taskId: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val note: String = "",
    val dueDate: String = "",
    val priority: String = "Imp",
    
    @get:PropertyName("completed")
    @set:PropertyName("completed")
    @PropertyName("completed")
    var isCompleted: Boolean = false,
    
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "taskId" to taskId,
            "userId" to userId,
            "subjectId" to subjectId,
            "subjectName" to subjectName,
            "title" to title,
            "note" to note,
            "dueDate" to dueDate,
            "priority" to priority,
            "completed" to isCompleted,
            "createdAt" to createdAt
        )
    }
}