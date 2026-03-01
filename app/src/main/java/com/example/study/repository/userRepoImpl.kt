package com.example.study.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.study.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class userRepoImpl : UserRepo {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var ref: DatabaseReference = database.getReference("users")

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val fileName = getFileNameFromUri(context, imageUri) ?: "profile_image"
        
        MediaManager.get().upload(imageUri)
            .option("public_id", fileName.substringBeforeLast("."))
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = (resultData?.get("secure_url") ?: resultData?.get("url")) as String?
                    callback(imageUrl)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    callback(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName ?: uri.path?.substringAfterLast('/')
    }

    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Login Successful")
            else callback(false, "${it.exception?.message}")
        }
    }

    override fun register(name: String, email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Registration Successful", "${auth.currentUser?.uid}")
            else callback(false, "${it.exception?.message}", "")
        }
    }

    override fun addUserToDatabase(userId: String, model: User, callback: (Boolean, String) -> Unit) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "User added to database")
            else callback(false, "${it.exception?.message}")
        }
    }

    override fun updateProfile(userId: String, model: User, callback: (Boolean, String) -> Unit) {
        ref.child(userId).updateChildren(model.toMap()).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Profile updated")
            else callback(false, "${it.exception?.message}")
        }
    }

    override fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit) {
        ref.child(userId).removeValue().addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Account deleted")
            else callback(false, "${it.exception?.message}")
        }
    }

    override fun getUserById(userId: String, callback: (Boolean, String, User?) -> Unit) {
        ref.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userClass = User::class.java
                    val user = snapshot.getValue(userClass)
                    if (user != null) callback(true, "Profile fetched", user)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, null)
            }
        })
    }

    override fun getAllUser(callback: (Boolean, String, List<User>?) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allUsers = mutableListOf<User>()
                val userClass = User::class.java
                for (data in snapshot.children) {
                    val user = data.getValue(userClass)
                    if (user != null) allUsers.add(user)
                }
                callback(true, "Users fetched", allUsers)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun logOut(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true, "Logout successfully")
        } catch (e: Exception) {
            callback(false, "${e.message}")
        }
    }

    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) callback(true, "Email sent")
            else callback(false, "${it.exception?.message}")
        }
    }
}
