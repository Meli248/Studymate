package com.example.study

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.study.model.User
import com.example.study.repository.userRepoImpl
import com.example.study.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Profile() {
    val context = LocalContext.current
    val activity = context as Activity

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid ?: ""
    val userEmail = currentUser?.email ?: ""

    val userViewModel = remember { UserViewModel(userRepoImpl()) }

    val user by userViewModel.users.observeAsState()
    val isLoading by userViewModel.loading.observeAsState(false)

    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    LaunchedEffect(user) {
        user?.let {
            name = it.fullName
            course = it.course
            semester = it.semester
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------------- PROFILE HEADER ---------------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightGreen)
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(PrimaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = name.ifEmpty { "Student" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Text(
                    text = userEmail,
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* ---------------- PERSONAL INFO CARD ---------------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Text(
                    text = "Personal Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Name field with required asterisk
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Name", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GrayText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "*", fontSize = 14.sp, fontWeight = FontWeight.Medium,color = Color(0xFF7C2929))
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoRow(
                    icon = R.drawable.baseline_person_24,
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Enter your name"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email field with required asterisk (read-only)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GrayText)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF7C2929))
                }
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoRow(
                    icon = R.drawable.baseline_email_24,
                    value = userEmail,
                    onValueChange = {},
                    enabled = false,
                    placeholder = "Email"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Course field (optional)
                Text(text = "Course (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GrayText)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoRow(
                    icon = R.drawable.baseline_menu_book_24,
                    value = course,
                    onValueChange = { course = it },
                    placeholder = "e.g., Computer Science"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Semester field (optional)
                Text(text = "Semester (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = GrayText)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileInfoRow(
                    icon = R.drawable.baseline_school_24,
                    value = semester,
                    onValueChange = { semester = it },
                    placeholder = "e.g., 3rd Semester"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val updatedUser = User(
                            id = userId,
                            fullName = name,
                            email = userEmail,
                            profileImage = "",
                            course = course,
                            semester = semester
                        )

                        userViewModel.updateProfile(userId, updatedUser) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(text = "Update Profile", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ---------------- ACTIONS CARD ---------------- */
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            userViewModel.logOut { success, message ->
                                if (success) {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, Login::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                    activity.finish()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Logout", fontSize = 16.sp, color = DarkText, fontWeight = FontWeight.Medium)
                }

                Divider(color = FieldGray, thickness = 1.dp)

                // Delete Account
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = null,
                        tint = Color(0xFF7C2929),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Delete Account", fontSize = 16.sp, color = Color(0xFF7C2929), fontWeight = FontWeight.Medium)
                }
            }
        }

        // Removed large gap — just a small bottom padding
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Delete Account Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = "Delete Account", fontWeight = FontWeight.Bold, color = DarkText)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently deleted.",
                    color = GrayText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deleteAccount(userId) { success, message ->
                            if (success) {
                                currentUser?.delete()?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(context, Login::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        context.startActivity(intent)
                                        activity.finish()
                                    } else {
                                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C2929))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = PrimaryGreen)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/* ---------------- PROFILE INFO ROW ---------------- */
@Composable
fun ProfileInfoRow(
    icon: Int,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String = ""
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(LightGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            placeholder = {
                Text(
                    text = placeholder.ifEmpty { "Enter value" },
                    color = GrayText.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldGray,
                unfocusedContainerColor = FieldGray,
                disabledContainerColor = FieldGray.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                disabledTextColor = DarkText.copy(alpha = 0.6f)
            )
        )
    }
}