package com.example.study.repository

import com.example.study.model.Task

interface TaskRepo {

    fun addTask(
        taskId: String,
        model: Task,
        callback: (Boolean, String) -> Unit
    )

    fun updateTask(
        taskId: String,
        model: Task,
        callback: (Boolean, String) -> Unit
    )

    fun deleteTask(
        taskId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getTaskById(
        taskId: String,
        callback: (Boolean, String, Task?) -> Unit
    )

    fun getAllTasks(
        userId: String,
        callback: (Boolean, String, List<Task>?) -> Unit
    )

    fun getTasksBySubject(
        userId: String,
        subjectId: String,
        callback: (Boolean, String, List<Task>?) -> Unit
    )

    fun toggleTaskCompletion(
        taskId: String,
        isCompleted: Boolean,
        callback: (Boolean, String) -> Unit
    )
}