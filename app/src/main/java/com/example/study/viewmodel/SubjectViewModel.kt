package com.example.study.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.study.model.Subject
import com.example.study.repository.SubjectRepo
import com.example.study.repository.SubjectRepoImpl
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SubjectViewModel(private val repo: SubjectRepo) : ViewModel() {

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects

    fun loadSubjects(userId: String) {
        repo.getAllSubjects(userId) { success, _, subjectList ->
            if (success && subjectList != null) {
                _subjects.value = subjectList
            }
        }
    }

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
            callback(success, message, if (success) subjectId else null)
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
        _subjects.value = _subjects.value.filter { it.subjectId != subjectId }
        repo.deleteSubject(subjectId, callback)
    }

    fun getAllSubjects(
        userId: String,
        callback: (Boolean, String, List<Subject>?) -> Unit
    ) {
        repo.getAllSubjects(userId, callback)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SubjectViewModel(SubjectRepoImpl()) as T
            }
        }
    }
}