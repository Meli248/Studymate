package com.example.study

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.study.model.Subject
import com.example.study.model.Task
import com.example.study.repository.SubjectRepoImpl
import com.example.study.repository.TaskRepoImpl
import com.example.study.viewmodel.SubjectViewModel
import com.example.study.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Subject() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val subjectViewModel = remember { SubjectViewModel(SubjectRepoImpl()) }
    val taskViewModel = remember { TaskViewModel(TaskRepoImpl()) }

    var subjects by remember { mutableStateOf(listOf<Subject>()) }
    var tasksMap by remember { mutableStateOf(mapOf<String, List<Task>>()) }
    var subjectName by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    // Load subjects from Firebase
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            subjectViewModel.getAllSubjects(userId) { success, message, subjectList ->
                if (success && subjectList != null) {
                    subjects = subjectList

                    // Load tasks for each subject
                    subjectList.forEach { subject ->
                        taskViewModel.getAllTasks(userId) { taskSuccess, _, taskList ->
                            if (taskSuccess && taskList != null) {
                                val subjectTasks = taskList.filter { it.subjectId == subject.subjectId }
                                tasksMap = tasksMap + (subject.subjectId to subjectTasks)
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(16.dp)
        ) {
            // Add Subject Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    placeholder = { Text("Enter subject name", color = GrayText.copy(alpha = 0.6f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                // Add Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryGreen, RoundedCornerShape(12.dp))
                        .clickable {
                            if (subjectName.isNotBlank()) {
                                subjectViewModel.addSubject(userId, subjectName) { success, message, subjectId ->
                                    if (success) {
                                        subjectName = ""
                                        Toast
                                            .makeText(context, message, Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast
                                            .makeText(context, message, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            } else {
                                Toast
                                    .makeText(context, "Please enter subject name", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Add Subject",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subjects List
            if (subjects.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No subjects yet. Add your first subject!",
                            color = GrayText,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(subjects) { subject ->
                        val subjectTasks = tasksMap[subject.subjectId] ?: emptyList()
                        SubjectCard(
                            subject = subject,
                            tasks = subjectTasks,
                            onEdit = {
                                editingSubject = subject
                                showEditDialog = true
                            },
                            onDelete = {
                                subjectViewModel.deleteSubject(subject.subjectId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Edit Subject Dialog
    if (showEditDialog && editingSubject != null) {
        EditSubjectDialog(
            subject = editingSubject!!,
            onDismiss = {
                showEditDialog = false
                editingSubject = null
            },
            onUpdate = { updatedName ->
                val updatedSubject = editingSubject!!.copy(name = updatedName)
                subjectViewModel.updateSubject(editingSubject!!.subjectId, updatedSubject) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showEditDialog = false
                        editingSubject = null
                    }
                }
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: Subject,
    tasks: List<Task>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (tasks.isNotEmpty()) expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(LightGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_menu_book_24),
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkText
                    )
                    Text(
                        text = "${tasks.size} task${if (tasks.size != 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(40.dp)
                        .background(LightGreen, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Edit",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PendingRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Delete",
                        tint = PendingRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Show tasks if expanded and has tasks
            if (expanded && tasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (task.isCompleted) LightGreen.copy(alpha = 0.5f)
                                else LightGreen.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (task.isCompleted) R.drawable.baseline_check_circle_24
                                else R.drawable.baseline_access_time_24
                            ),
                            contentDescription = null,
                            tint = if (task.isCompleted) PrimaryGreen else GrayText,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (task.isCompleted) GrayText else DarkText
                            )
                            Text(
                                text = "📅 ${task.dueDate}",
                                fontSize = 12.sp,
                                color = GrayText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun EditSubjectDialog(
    subject: Subject,
    onDismiss: () -> Unit,
    onUpdate: (String) -> Unit
) {
    var subjectName by remember { mutableStateOf(subject.name) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Edit Subject",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Subject Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    placeholder = { Text("Enter subject name", color = GrayText.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FieldGray,
                        unfocusedContainerColor = FieldGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FieldGray,
                            contentColor = DarkText
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (subjectName.isBlank()) {
                                Toast.makeText(context, "Please enter subject name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onUpdate(subjectName)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(
                            text = "Update",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewSubject() {
    Subject()
}