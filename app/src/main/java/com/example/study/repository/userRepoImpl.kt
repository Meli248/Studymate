package com.example.study.repository

import com.example.study.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class userRepoImpl: UserRepo
{
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var ref: DatabaseReference = database.getReference("users")


    override fun login(
        email: String,
        password: String,
        callback: (Boolean, String) -> Unit
    ) {
      auth.signInWithEmailAndPassword(email,password)
          .addOnCompleteListener {
          if (it.isSuccessful){
              callback(true, "Login Successful")
          } else {
              callback(false, "${it.exception?.message}")
          }

      }
    }

    override fun register(
        name: String,
        email: String,
        password: String,
        callback: (Boolean, String, String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Registration Successful", "${auth.currentUser?.uid}")
                } else {
                    callback(false, "${it.exception?.message}", "")
                }
            }
    }


    override fun addUserToDatabase(
        userId: String,
        model: User,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).setValue(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "User added to database")
                } else {
                    callback(false, "${it.exception?.message}")
                }

            }
    }

    override fun updateProfile(
        userId: String,
        model: User,
        callback: (Boolean, String) -> Unit
    ) {

        ref.child(userId).updateChildren(model.toMap())
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(true, "Profile updated")
                } else {
                    callback(false, "${it.exception?.message}")
                }
            }
    }

    override fun deleteAccount(
        userId: String,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(userId).removeValue().addOnCompleteListener {
        if (it.isSuccessful) {
            callback(true, "Account deleted")
        } else {
            callback(false, "${it.exception?.message}")
        }
    }

    }

    override fun getUserById(
        userId: String,
        callback: (Boolean, String, User?) -> Unit
    ) {
        ref.child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val users = snapshot.getValue(User::class.java)
                        if (users != null) {
                            callback(true, "Profile fetched", users)
                        }
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
                if (snapshot.exists()) {
                    val allUsers = mutableListOf<User>()
                    for (data in snapshot.children) {
                        val users = data.getValue(User::class.java)
                        if (users != null) {
                            allUsers.add(users)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

                    override fun getCurrentUser(): FirebaseUser? {
                return auth.currentUser
            }

                    override fun logOut(callback: (Boolean, String) -> Unit) {
                try {
                    auth.signOut()
                    callback(true, "logout successfully")
                } catch (e: Exception) {
                    callback(false, "${e.message}")
                }
            }

                    override fun forgetPassword(
                email: String,
                callback: (Boolean, String) -> Unit
            ) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            callback(true, "Email sent")
                        } else {
                            callback(false, "${it.exception?.message}")
                        }
                    }
                }
    }