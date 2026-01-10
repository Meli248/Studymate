package com.example.study.repository

import com.example.study.model.Subject

interface SubjectRepo {

    fun addSubject(
        subjectId: String,
        model: Subject,
        callback: (Boolean, String) -> Unit
    )

    fun updateSubject(
        subjectId: String,
        model: Subject,
        callback: (Boolean, String) -> Unit
    )

    fun deleteSubject(
        subjectId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getSubjectById(
        subjectId: String,
        callback: (Boolean, String, Subject?) -> Unit
    )

    fun getAllSubjects(
        userId: String,
        callback: (Boolean, String, List<Subject>?) -> Unit
    )
}