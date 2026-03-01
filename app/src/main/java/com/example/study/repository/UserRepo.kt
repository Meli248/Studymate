package com.example.study.repository

import android.content.Context
import android.net.Uri
import com.example.study.model.User
import com.google.firebase.auth.FirebaseUser

interface UserRepo {
    fun login(email: String, password: String, callback: (Boolean, String) -> Unit)
    fun register(name: String, email: String, password: String, callback: (Boolean, String, String) -> Unit)
    fun addUserToDatabase(userId: String, model: User, callback: (Boolean, String) -> Unit)
    fun updateProfile(userId: String, model: User, callback: (Boolean, String) -> Unit)
    fun deleteAccount(userId: String, callback: (Boolean, String) -> Unit)
    fun getUserById(userId: String, callback: (Boolean, String, User?) -> Unit)
    fun getAllUser(callback: (Boolean, String, List<User>?) -> Unit)
    fun getCurrentUser(): FirebaseUser?
    fun logOut(callback: (Boolean, String) -> Unit)
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit)

    // Cloudinary methods
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit)
    fun getFileNameFromUri(context: Context, uri: Uri): String?
}
