package com.example.study.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.study.model.Task
import com.example.study.repository.TaskRepo
import com.example.study.repository.TaskRepoImpl
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel(private val repo: TaskRepo) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    // Track pending completion writes so Firebase doesn't overwrite them
    private val pendingCompletions = mutableMapOf<String, Boolean>()

    fun loadTasks(userId: String) {
        repo.getAllTasks(userId) { success, _, taskList ->
            if (success && taskList != null) {
                // Merge: for each task, if we have a pending completion change, keep it
                val merged = taskList.map { task ->
                    if (pendingCompletions.containsKey(task.taskId)) {
                        task.copy(isCompleted = pendingCompletions[task.taskId]!!)
                    } else {
                        task
                    }
                }
                _tasks.value = merged
            }
        }
    }

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
            taskId = taskId, userId = userId, subjectId = subjectId,
            subjectName = subjectName, title = title, note = note,
            dueDate = dueDate, priority = priority, isCompleted = false,
            createdAt = System.currentTimeMillis()
        )
        repo.addTask(taskId, task) { success, message ->
            callback(success, message, if (success) taskId else null)
        }
    }

    fun updateTask(taskId: String, task: Task, callback: (Boolean, String) -> Unit) {
        repo.updateTask(taskId, task, callback)
    }

    fun deleteTask(taskId: String, callback: (Boolean, String) -> Unit) {
        // Remove from pending too
        pendingCompletions.remove(taskId)
        // Remove locally immediately
        _tasks.value = _tasks.value.filter { it.taskId != taskId }
        repo.deleteTask(taskId, callback)
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean, callback: (Boolean, String) -> Unit) {
        // Track as pending so Firebase listener won't overwrite it
        pendingCompletions[taskId] = isCompleted
        // Apply optimistically
        _tasks.value = _tasks.value.map { t ->
            if (t.taskId == taskId) t.copy(isCompleted = isCompleted) else t
        }
        repo.toggleTaskCompletion(taskId, isCompleted) { success, message ->
            if (success) {
                // Write confirmed — remove from pending
                pendingCompletions.remove(taskId)
            }
            callback(success, message)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TaskViewModel(TaskRepoImpl()) as T
            }
        }
    }
}