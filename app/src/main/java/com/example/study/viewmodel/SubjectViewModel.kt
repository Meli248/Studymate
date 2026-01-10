package com.example.study.viewmodel

import com.example.study.model.Subject
import com.example.study.repository.SubjectRepo
import com.google.firebase.database.FirebaseDatabase

class SubjectViewModel(private val repo: SubjectRepo) {

    fun addSubject(
        userId: String,
        name: String,
        callback: (Boolean, String, String?) -> Unit
    ) {
        val subjectId = FirebaseDatabase.getInstance().reference.push().key ?: ""
        val subject = Subject(
            subjectId = subjectId,
            userId = userId,
            name = name,
            createdAt = System.currentTimeMillis()
        )

        repo.addSubject(subjectId, subject) { success, message ->
            if (success) {
                callback(true, message, subjectId)
            } else {
                callback(false, message, null)
            }
        }
    }

    fun updateSubject(
        subjectId: String,
        subject: Subject,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateSubject(subjectId, subject, callback)
    }

    fun deleteSubject(
        subjectId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteSubject(subjectId, callback)
    }

    fun getAllSubjects(
        userId: String,
        callback: (Boolean, String, List<Subject>?) -> Unit
    ) {
        repo.getAllSubjects(userId, callback)
    }
}