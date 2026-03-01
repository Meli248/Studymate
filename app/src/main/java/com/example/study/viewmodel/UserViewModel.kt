package com.example.study.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.study.model.User
import com.example.study.repository.UserRepo
import com.google.firebase.auth.FirebaseUser

class UserViewModel (val repo: UserRepo): ViewModel()  {

    fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.login(email, password, callback)
    }

    fun register(
        name: String,
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        repo.register(name,email, password, callback)
    }

    fun addUserToDatabase(
        userId: String,
        model: User,
        callback: (Boolean, String) -> Unit
    ) {
        repo.addUserToDatabase(userId, model, callback)

    }

    fun updateProfile(
        userId: String,
        model: User,
        callback: (Boolean, String) -> Unit
    ) {
        repo.updateProfile(userId, model, callback)
    }

    fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        repo.deleteAccount(userId, callback)
    }

    private val _users = MutableLiveData<User?>()
    val users: MutableLiveData<User?> get() = _users

    private val _allUsers = MutableLiveData<List<User>?>()
    val allUsers: MutableLiveData<List<User>?> get() = _allUsers

    private val _loading= MutableLiveData<Boolean>()
    val loading: MutableLiveData<Boolean> get() = _loading


    fun getUserById(userId: String)
    {
        _loading.postValue(true)
        repo.getUserById(userId)
        { success, message, data ->
            if (success) {
                _loading.postValue(false)
                _users.postValue(data)
            } else {
                _loading.postValue(false)
                _users.postValue(null)
            }

        }

    }



    fun getAllUser() {
        _loading.postValue(true)
        repo.getAllUser {
                success, message, data ->
            if (success) {
                _loading.postValue(false)
                _allUsers.postValue(data)
            } else {
                _loading.postValue(false)
                _allUsers.postValue(emptyList())
            }
        }

    }

    fun getCurrentUser(): FirebaseUser? {
        return repo.getCurrentUser()
    }

    fun logOut(callback: (Boolean, String) -> Unit) {
        repo.logOut(callback)
    }

    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }

    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        _loading.postValue(true)
        repo.uploadImage(context, imageUri) { url ->
            _loading.postValue(false)
            callback(url)
        }
    }
}
