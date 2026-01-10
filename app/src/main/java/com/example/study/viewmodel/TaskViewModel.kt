package com.example.study.viewmodel

import com.example.study.model.Task
import com.example.study.repository.TaskRepo
import com.google.firebase.database.FirebaseDatabase

class TaskViewModel(private val repo: TaskRepo) {

    fun addTask(
        userId: String,
        subjectId: String,
        subjectName: String,
        title: String,
        note: String,
        dueDate: String,
        priority: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val taskId = FirebaseDatabase.getInstance().reference.push().key ?: ""
        val task = Task(
            taskId = taskId,
            userId = userId,
            subjectId = subjectId,
            subjectName = subjectName,
            title = title,
            note = note,
            dueDate = dueDate,
            priority = priority,
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )

        repo.addTask(taskId, task) { success, message ->
            if (success) {
                callback(true, message, taskId)
            } else {
                callback(false, message, null)
            }
        }
    }

    fun updateTask(
        taskId: String,
        task: Task,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateTask(taskId, task, callback)
    }

    fun deleteTask(
        taskId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteTask(taskId, callback)
    }

    fun getAllTasks(
        userId: String,
        callback: (Boolean, String, List<Task>?) -> Unit
    ) {
        repo.getAllTasks(userId, callback)
    }

    fun toggleTaskCompletion(
        taskId: String,
        isCompleted: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        repo.toggleTaskCompletion(taskId, isCompleted, callback)
    }
}