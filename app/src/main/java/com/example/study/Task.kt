package com.example.study

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextDecoration
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Task() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val taskViewModel = remember { TaskViewModel(TaskRepoImpl()) }
    val subjectViewModel = remember { SubjectViewModel(SubjectRepoImpl()) }

    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var subjects by remember { mutableStateOf(listOf<Subject>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            taskViewModel.getAllTasks(userId) { success, _, taskList ->
                if (success && taskList != null) {
                    tasks = taskList.sortedWith(
                        compareBy<Task> { it.isCompleted }.thenBy { parseDateString(it.dueDate) }
                    )
                }
            }

            subjectViewModel.getAllSubjects(userId) { success, _, subjectList ->
                if (success && subjectList != null) {
                    subjects = subjectList
                }
            }
        }
    }

    // Optimistically toggle task in local state so UI responds immediately
    fun handleToggle(taskId: String, currentIsCompleted: Boolean) {
        val newValue = !currentIsCompleted
        tasks = tasks.map { t ->
            if (t.taskId == taskId) t.copy(isCompleted = newValue) else t
        }.sortedWith(compareBy<Task> { it.isCompleted }.thenBy { parseDateString(it.dueDate) })
        taskViewModel.toggleTaskCompletion(taskId, newValue) { _, _ -> }
    }

    fun handleDelete(taskId: String) {
        tasks = tasks.filter { it.taskId != taskId }
        taskViewModel.deleteTask(taskId) { _, message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    val todayTasks = tasks.filter { !it.isCompleted && (it.dueDate == today || it.dueDate.contains("Today")) }
    val upcomingTasks = tasks.filter { !it.isCompleted && it.dueDate != today && !it.dueDate.contains("Today") }
    val completedTasks = tasks.filter { it.isCompleted }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
        ) {
            if (todayTasks.isNotEmpty()) {
                items(todayTasks, key = { it.taskId }) { task ->
                    TaskItemCard(
                        task = task,
                        isOverdue = true,
                        onToggleComplete = { taskId, isCompleted ->
                            handleToggle(taskId, isCompleted)
                        },
                        onEdit = { editingTask = task; showEditDialog = true },
                        onDelete = { handleDelete(task.taskId) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            items(upcomingTasks, key = { it.taskId }) { task ->
                TaskItemCard(
                    task = task,
                    isOverdue = false,
                    onToggleComplete = { taskId, isCompleted ->
                        handleToggle(taskId, isCompleted)
                    },
                    onEdit = { editingTask = task; showEditDialog = true },
                    onDelete = { handleDelete(task.taskId) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            items(completedTasks, key = { it.taskId }) { task ->
                TaskItemCard(
                    task = task,
                    isOverdue = false,
                    onToggleComplete = { taskId, isCompleted ->
                        handleToggle(taskId, isCompleted)
                    },
                    onEdit = { editingTask = task; showEditDialog = true },
                    onDelete = { handleDelete(task.taskId) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            if (tasks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks yet. Create your first task!",
                                color = GrayText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp),
            containerColor = PrimaryGreen,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = "Add Task",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAddTask = { title, subjectId, subjectName, dueDate, note, priority ->
                taskViewModel.addTask(
                    userId = userId,
                    subjectId = subjectId,
                    subjectName = subjectName,
                    title = title,
                    note = note,
                    dueDate = dueDate,
                    priority = priority
                ) { success, message, _ ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) showAddDialog = false
                }
            }
        )
    }

    if (showEditDialog && editingTask != null) {
        EditTaskDialog(
            task = editingTask!!,
            subjects = subjects,
            onDismiss = {
                showEditDialog = false
                editingTask = null
            },
            onUpdate = { title, subjectId, subjectName, dueDate, note, priority ->
                val updatedTask = editingTask!!.copy(
                    title = title,
                    subjectId = subjectId,
                    subjectName = subjectName,
                    dueDate = dueDate,
                    note = note,
                    priority = priority
                )
                taskViewModel.updateTask(editingTask!!.taskId, updatedTask) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showEditDialog = false
                        editingTask = null
                    }
                }
            }
        )
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
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isOverdue && !task.isCompleted)
                    Modifier.border(2.dp, Color(0xFFE53935), RoundedCornerShape(16.dp))
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = { onToggleComplete(task.taskId, task.isCompleted) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (task.isCompleted) R.drawable.baseline_check_circle_24
                        else R.drawable.baseline_radio_button_unchecked_24
                    ),
                    contentDescription = null,
                    tint = if (task.isCompleted) PrimaryGreen
                    else if (isOverdue) Color(0xFFE53935)
                    else GrayText,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task info — title, subject badge, then date below
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) GrayText else DarkText,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Subject badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LightGreen
                ) {
                    Text(
                        text = task.subjectName,
                        fontSize = 12.sp,
                        color = PrimaryGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date row — always on its own line, single line guaranteed
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                        contentDescription = null,
                        tint = if (isOverdue && !task.isCompleted) Color(0xFFE53935) else GrayText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dueDate,
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = if (isOverdue && !task.isCompleted) Color(0xFFE53935) else GrayText
                    )
                }
            }

            // Edit button
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

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0x1F7C2929), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_delete_24),
                    contentDescription = "Delete",
                    tint = Color(0xFF7C2929),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onAddTask: (String, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var taskTitle by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Imp") }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(y, m, d)
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                selectedDate = sdf.format(calendar.time)
            },
            year, month, day
        )
    }

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
                    text = "New Task",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Task Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    placeholder = { Text("Enter task title", color = GrayText.copy(alpha = 0.5f)) },
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Subject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (subjects.isEmpty()) {
                    Text(
                        text = "No subjects available. Add subjects first.",
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.take(3).forEach { subject ->
                            FilterChip(
                                selected = selectedSubject?.subjectId == subject.subjectId,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject.name, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = LightGreen,
                                    labelColor = PrimaryGreen
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Due Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = selectedDate.ifEmpty {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                        },
                        onValueChange = {},
                        enabled = false,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = FieldGray,
                            disabledIndicatorColor = Color.Transparent,
                            disabledTextColor = DarkText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Note (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add a note or description...", color = GrayText.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
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
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FieldGray,
                            contentColor = DarkText
                        )
                    ) {
                        Text(text = "Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            if (taskTitle.isBlank()) {
                                Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedSubject == null) {
                                Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalDueDate = selectedDate.ifEmpty {
                                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
                            }
                            onAddTask(taskTitle, selectedSubject!!.subjectId, selectedSubject!!.name, finalDueDate, note, priority)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(text = "Add Task", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var taskTitle by remember { mutableStateOf(task.title) }
    var selectedSubject by remember { mutableStateOf(subjects.find { it.subjectId == task.subjectId }) }
    var selectedDate by remember { mutableStateOf(task.dueDate) }
    var note by remember { mutableStateOf(task.note) }
    var priority by remember { mutableStateOf(task.priority) }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                calendar.set(y, m, d)
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                selectedDate = sdf.format(calendar.time)
            },
            year, month, day
        )
    }

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
                    text = "Edit Task",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Task Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    placeholder = { Text("Enter task title", color = GrayText.copy(alpha = 0.5f)) },
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

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Subject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "*", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (subjects.isEmpty()) {
                    Text(
                        text = "No subjects available. Add subjects first.",
                        fontSize = 12.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.take(3).forEach { subject ->
                            FilterChip(
                                selected = selectedSubject?.subjectId == subject.subjectId,
                                onClick = { selectedSubject = subject },
                                label = { Text(subject.name, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryGreen,
                                    selectedLabelColor = Color.White,
                                    containerColor = LightGreen,
                                    labelColor = PrimaryGreen
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Due Date",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {},
                        enabled = false,
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                                contentDescription = null,
                                tint = PrimaryGreen
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            disabledContainerColor = FieldGray,
                            disabledIndicatorColor = Color.Transparent,
                            disabledTextColor = DarkText
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Note (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkText
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Add a note or description...", color = GrayText.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
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
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FieldGray,
                            contentColor = DarkText
                        )
                    ) {
                        Text(text = "Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            if (taskTitle.isBlank()) {
                                Toast.makeText(context, "Please enter task title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedSubject == null) {
                                Toast.makeText(context, "Please select a subject", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onUpdate(
                                taskTitle,
                                selectedSubject!!.subjectId,
                                selectedSubject!!.name,
                                selectedDate,
                                note,
                                priority
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text(text = "Update", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1)
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