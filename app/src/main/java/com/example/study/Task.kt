package com.example.study

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.study.model.Subject
import com.example.study.model.Task
import com.example.study.viewmodel.SubjectViewModel
import com.example.study.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Task(
    taskViewModel: TaskViewModel,
    subjectViewModel: SubjectViewModel
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val tasks by taskViewModel.tasks.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    fun handleToggle(taskId: String, currentIsCompleted: Boolean) {
        taskViewModel.toggleTaskCompletion(taskId, !currentIsCompleted) { _, _ -> }
    }

    // Only delete the specific task by ID
    fun handleDelete(taskId: String) {
        taskViewModel.deleteTask(taskId) { _, message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    val sortedTasks = tasks.sortedWith(compareBy<Task> { it.isCompleted }.thenBy { parseDateString(it.dueDate) })
    val todayTasks = sortedTasks.filter { !it.isCompleted && (it.dueDate == today || it.dueDate.contains("Today")) }
    val upcomingTasks = sortedTasks.filter { !it.isCompleted && it.dueDate != today && !it.dueDate.contains("Today") }
    val completedTasks = sortedTasks.filter { it.isCompleted }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().background(Background)) {
            if (todayTasks.isNotEmpty()) {
                items(todayTasks, key = { it.taskId }) { task ->
                    TaskItemCard(task, true, { id, comp -> handleToggle(id, comp) }, { editingTask = task; showEditDialog = true }, { handleDelete(task.taskId) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
                }
            }
            items(upcomingTasks, key = { it.taskId }) { task ->
                TaskItemCard(task, false, { id, comp -> handleToggle(id, comp) }, { editingTask = task; showEditDialog = true }, { handleDelete(task.taskId) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            items(completedTasks, key = { it.taskId }) { task ->
                TaskItemCard(task, false, { id, comp -> handleToggle(id, comp) }, { editingTask = task; showEditDialog = true }, { handleDelete(task.taskId) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
            if (tasks.isEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No tasks yet. Create your first task!", color = GrayText, fontSize = 14.sp)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(35.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 80.dp, end = 16.dp),
            containerColor = PrimaryGreen, contentColor = Color.White, shape = CircleShape
        ) {
            Icon(painterResource(R.drawable.baseline_add_24), "Add Task", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddDialog) {
        AddTaskDialog(subjects, onDismiss = { showAddDialog = false }) { title, subjectId, subjectName, dueDate, note, priority ->
            taskViewModel.addTask(userId, subjectId, subjectName, title, note, dueDate, priority) { success, message, _ ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (success) showAddDialog = false
            }
        }
    }

    if (showEditDialog && editingTask != null) {
        EditTaskDialog(editingTask!!, subjects, onDismiss = { showEditDialog = false; editingTask = null }) { title, subjectId, subjectName, dueDate, note, priority ->
            val updated = editingTask!!.copy(title = title, subjectId = subjectId, subjectName = subjectName, dueDate = dueDate, note = note, priority = priority)
            taskViewModel.updateTask(editingTask!!.taskId, updated) { success, message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (success) { showEditDialog = false; editingTask = null }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    isOverdue: Boolean,
    onToggleComplete: (String, Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth().then(if (isOverdue && !task.isCompleted) Modifier.border(2.dp, Color(0xFF7C2929), RoundedCornerShape(16.dp)) else Modifier)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            IconButton(onClick = { onToggleComplete(task.taskId, task.isCompleted) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    painterResource(if (task.isCompleted) R.drawable.baseline_check_circle_24 else R.drawable.baseline_radio_button_unchecked_24),
                    null,
                    tint = if (task.isCompleted) PrimaryGreen else if (isOverdue) Color(0xFF7C2929) else GrayText,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (task.isCompleted) GrayText else DarkText, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = LightGreen) {
                    Text(task.subjectName, fontSize = 12.sp, color = PrimaryGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Medium)
                }
                if (task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(task.note, fontSize = 12.sp, color = GrayText, maxLines = 2, lineHeight = 16.sp)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_calendar_month_24), null, tint = if (isOverdue && !task.isCompleted) Color(0xFF7C2929) else GrayText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(task.dueDate, fontSize = 12.sp, color = if (isOverdue && !task.isCompleted) Color(0xFF7C2929) else GrayText)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp).background(LightGreen, RoundedCornerShape(10.dp))) {
                Icon(painterResource(R.drawable.baseline_edit_24), "Edit", tint = PrimaryGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp).background(Color(0xFF7C2929).copy(alpha = 0.12f), RoundedCornerShape(10.dp))) {
                Icon(painterResource(R.drawable.baseline_delete_24), "Delete", tint = Color(0xFF7C2929), modifier = Modifier.size(20.dp))
            }        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(subjects: List<Subject>, onDismiss: () -> Unit, onAddTask: (String, String, String, String, String, String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var taskTitle by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            calendar.set(y, m, d)
            selectedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text("New Task", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Task Title", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = taskTitle, onValueChange = { taskTitle = it },
                    placeholder = { Text("Enter task title", color = GrayText.copy(alpha = 0.5f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = FieldGray, unfocusedContainerColor = FieldGray, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Subject", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                    Text(" *", fontSize = 14.sp, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (subjects.isEmpty()) {
                    Text("No subjects available. Add subjects first.", fontSize = 12.sp, color = Color.Red)
                } else {
                    // Horizontal scroll — all subjects visible in one line, no text wrapping
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { subject ->
                            FilterChip(
                                selected = selectedSubject?.subjectId == subject.subjectId,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject.name, fontSize = 13.sp, maxLines = 1, softWrap = false) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White, containerColor = LightGreen, labelColor = PrimaryGreen),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Due Date", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                    OutlinedTextField(
                        value = selectedDate.ifEmpty { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()) },
                        onValueChange = {}, enabled = false, singleLine = true,
                        leadingIcon = { Icon(painterResource(R.drawable.baseline_calendar_month_24), null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(disabledContainerColor = FieldGray, disabledIndicatorColor = Color.Transparent, disabledTextColor = DarkText)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Note (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    placeholder = { Text("Add a note...", color = GrayText.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = FieldGray, unfocusedContainerColor = FieldGray, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FieldGray, contentColor = DarkText), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = {
                            if (taskTitle.isBlank()) { Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show(); return@Button }
                            if (selectedSubject == null) { Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show(); return@Button }
                            val finalDate = selectedDate.ifEmpty { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()) }
                            onAddTask(taskTitle, selectedSubject!!.subjectId, selectedSubject!!.name, finalDate, note, "Imp")
                        },
                        modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Add Task", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(task: Task, subjects: List<Subject>, onDismiss: () -> Unit, onUpdate: (String, String, String, String, String, String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var taskTitle by remember { mutableStateOf(task.title) }
    var selectedSubject by remember { mutableStateOf(subjects.find { it.subjectId == task.subjectId }) }
    var selectedDate by remember { mutableStateOf(task.dueDate) }
    var note by remember { mutableStateOf(task.note) }

    val datePickerDialog = remember {
        DatePickerDialog(context, { _, y, m, d ->
            calendar.set(y, m, d)
            selectedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Edit Task", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Task Title", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = taskTitle, onValueChange = { taskTitle = it },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = FieldGray, unfocusedContainerColor = FieldGray, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Subject *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                if (subjects.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { subject ->
                            FilterChip(
                                selected = selectedSubject?.subjectId == subject.subjectId,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject.name, fontSize = 13.sp, maxLines = 1, softWrap = false) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryGreen, selectedLabelColor = Color.White, containerColor = LightGreen, labelColor = PrimaryGreen),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Due Date", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                    OutlinedTextField(
                        value = selectedDate, onValueChange = {}, enabled = false, singleLine = true,
                        leadingIcon = { Icon(painterResource(R.drawable.baseline_calendar_month_24), null, tint = PrimaryGreen) },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(disabledContainerColor = FieldGray, disabledIndicatorColor = Color.Transparent, disabledTextColor = DarkText)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Note (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkText)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = FieldGray, unfocusedContainerColor = FieldGray, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = FieldGray, contentColor = DarkText), contentPadding = PaddingValues(horizontal = 8.dp)) {
                        Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = {
                            if (taskTitle.isBlank()) { Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show(); return@Button }
                            if (selectedSubject == null) { Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show(); return@Button }
                            onUpdate(taskTitle, selectedSubject!!.subjectId, selectedSubject!!.name, selectedDate, note, task.priority)
                        },
                        modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Update", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

fun parseDateString(dateStr: String): Date {
    return try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dateStr) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}