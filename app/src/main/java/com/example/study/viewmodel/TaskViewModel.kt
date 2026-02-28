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

    // Stores local completion states that MUST NOT be overwritten by Firebase re-fires.
    // Key = taskId, Value = isCompleted
    private val localCompletionOverrides = mutableMapOf<String, Boolean>()

    fun loadTasks(userId: String) {
        repo.getAllTasks(userId) { success, _, taskList ->
            if (success && taskList != null) {
                // Always apply local overrides on top of Firebase data.
                // This prevents Firebase listener re-fires from wiping optimistic UI updates.
                val merged = taskList.map { task ->
                    val localState = localCompletionOverrides[task.taskId]
                    if (localState != null) task.copy(isCompleted = localState) else task
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
        // Remove override so deleted task doesn't linger in the map
        localCompletionOverrides.remove(taskId)
        // Remove from list immediately so UI doesn't flash
        _tasks.value = _tasks.value.filter { it.taskId != taskId }
        repo.deleteTask(taskId, callback)
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean, callback: (Boolean, String) -> Unit) {
        // 1. Store override BEFORE Firebase write so any listener re-fires are already handled
        localCompletionOverrides[taskId] = isCompleted

        // 2. Apply immediately in UI (optimistic update)
        _tasks.value = _tasks.value.map { t ->
            if (t.taskId == taskId) t.copy(isCompleted = isCompleted) else t
        }

        // 3. Write to Firebase
        repo.toggleTaskCompletion(taskId, isCompleted) { success, message ->
            if (success) {
                // Firebase confirmed — remove override (Firebase value is now correct)
                localCompletionOverrides.remove(taskId)
            } else {
                // Revert on failure
                localCompletionOverrides.remove(taskId)
                _tasks.value = _tasks.value.map { t ->
                    if (t.taskId == taskId) t.copy(isCompleted = !isCompleted) else t
                }
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