package com.example.study.model

data class Task(
    val taskId: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val title: String = "",
    val note: String = "",
    val dueDate: String = "",
    val priority: String = "Imp",
    val isCompleted: Boolean = false,
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
            "isCompleted" to isCompleted,
            "createdAt" to createdAt
        )
    }
}