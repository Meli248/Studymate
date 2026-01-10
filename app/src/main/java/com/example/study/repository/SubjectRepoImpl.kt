package com.example.study.repository

import com.example.study.model.Subject
import com.google.firebase.database.*

class SubjectRepoImpl : SubjectRepo {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val ref: DatabaseReference = database.getReference("subjects")

    override fun addSubject(
        subjectId: String,
        model: Subject,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(subjectId).setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Subject added successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun updateSubject(
        subjectId: String,
        model: Subject,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(subjectId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Subject updated successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun deleteSubject(
        subjectId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(subjectId).removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Subject deleted successfully")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun getSubjectById(
        subjectId: String,
        callback: (Boolean, String, Subject?) -> Unit
    ) {
        ref.child(subjectId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val subject = snapshot.getValue(Subject::class.java)
                        callback(true, "Subject fetched successfully", subject)
                    } else {
                        callback(false, "Subject not found", null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }

    override fun getAllSubjects(
        userId: String,
        callback: (Boolean, String, List<Subject>?) -> Unit
    ) {
        // FIXED: Using addValueEventListener for real-time updates
        ref.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val subjects = mutableListOf<Subject>()
                    for (data in snapshot.children) {
                        val subject = data.getValue(Subject::class.java)
                        if (subject != null) {
                            subjects.add(subject)
                        }
                    }
                    if (subjects.isNotEmpty()) {
                        callback(true, "Subjects fetched successfully", subjects)
                    } else {
                        callback(true, "No subjects found", emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, error.message, null)
                }
            })
    }
}