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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.study.model.Subject
import com.example.study.model.Task
import com.example.study.viewmodel.SubjectViewModel
import com.example.study.viewmodel.TaskViewModel

@Composable
fun Subject(
    subjectViewModel: SubjectViewModel,
    taskViewModel: TaskViewModel
) {
    val context = LocalContext.current

    val subjects by subjectViewModel.subjects.collectAsState()
    val allTasks by taskViewModel.tasks.collectAsState()

    var subjectName by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }

    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    placeholder = { Text("Enter subject name", color = GrayText.copy(alpha = 0.6f)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryGreen, RoundedCornerShape(12.dp))
                        .clickable {
                            if (subjectName.isNotBlank()) {
                                subjectViewModel.addSubject(userId, subjectName) { success, message, _ ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) subjectName = ""
                                }
                            } else {
                                Toast.makeText(context, "Please enter subject name", Toast.LENGTH_SHORT).show()
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

            if (subjects.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No subjects yet. Add your first subject!", color = GrayText, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(subjects) { subject ->
                        val subjectTasks = allTasks.filter { it.subjectId == subject.subjectId }
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
                    item { Spacer(modifier = Modifier.height(35.dp)) }
                }
            }
        }
    }

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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (tasks.isNotEmpty()) expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    Text(text = subject.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = DarkText)
                    Text(
                        text = "${tasks.size} task${if (tasks.size != 1) "s" else ""}",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp).background(LightGreen, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Edit",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp).background(Color(0xFF7C2929).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Delete",
                        tint = Color(0xFF7C2929),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

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
                            Text(text = task.dueDate, fontSize = 12.sp, color = GrayText)
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
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Edit Subject", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(20.dp))

                Text("Subject Name", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    singleLine = true,
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
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FieldGray, contentColor = DarkText)
                    ) {
                        Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            if (subjectName.isBlank()) {
                                Toast.makeText(context, "Please enter subject name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onUpdate(subjectName)
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 56.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Update", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                    }
                }
            }
        }
    }
}