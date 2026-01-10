package com.example.study.repository

import com.example.study.model.Task
import com.google.firebase.database.*

class TaskRepoImpl : TaskRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("tasks")

    override fun addTask(
        taskId: String,
        model: Task,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(taskId).setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Task added successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun updateTask(
        taskId: String,
        model: Task,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(taskId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Task updated successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun deleteTask(
        taskId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(taskId).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Task deleted successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getTaskById(
        taskId: String,
        callback: (Boolean, String, Task?) -> Unit
    ) {
        ref.child(taskId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val task = snapshot.getValue(Task::class.java)
                        callback(true, "Task fetched successfully", task)
                    } else {
                        callback(false, "Task not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getAllTasks(
        userId: String,
        callback: (Boolean, String, List<Task>?) -> Unit
    ) {
        // FIXED: Using addValueEventListener for real-time updates
        ref.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    for (data in snapshot.children) {
                        val task = data.getValue(Task::class.java)
                        if (task != null) {
                            tasks.add(task)
                        }
                    }
                    if (tasks.isNotEmpty()) {
                        callback(true, "Tasks fetched successfully", tasks)
                    } else {
                        callback(true, "No tasks found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getTasksBySubject(
        userId: String,
        subjectId: String,
        callback: (Boolean, String, List<Task>?) -> Unit
    ) {
        // FIXED: Using addValueEventListener for real-time updates
        ref.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    for (data in snapshot.children) {
                        val task = data.getValue(Task::class.java)
                        if (task != null && task.subjectId == subjectId) {
                            tasks.add(task)
                        }
                    }
                    if (tasks.isNotEmpty()) {
                        callback(true, "Tasks fetched successfully", tasks)
                    } else {
                        callback(true, "No tasks found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun toggleTaskCompletion(
        taskId: String,
        isCompleted: Boolean,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(taskId).child("isCompleted").setValue(isCompleted)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Task status updated")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }
}