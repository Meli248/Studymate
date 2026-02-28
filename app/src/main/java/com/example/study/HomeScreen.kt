package com.example.study

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.study.model.Task
import com.example.study.repository.userRepoImpl
import com.example.study.ui.theme.CompletedCardGreen
import com.example.study.ui.theme.IconBackgroundBlue
import com.example.study.ui.theme.IconBackgroundGreen
import com.example.study.ui.theme.IconBackgroundOrange
import com.example.study.ui.theme.IconBackgroundSuccess
import com.example.study.ui.theme.PendingCardOrange
import com.example.study.ui.theme.SubjectCardGreen
import com.example.study.ui.theme.TaskCardBlue
import com.example.study.viewmodel.SubjectViewModel
import com.example.study.viewmodel.TaskViewModel
import com.example.study.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    taskViewModel: TaskViewModel,
    subjectViewModel: SubjectViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val userViewModel = remember { UserViewModel(userRepoImpl()) }

    // Collect shared state — updates instantly when tasks are toggled anywhere
    val tasks by taskViewModel.tasks.collectAsState()
    val subjects by subjectViewModel.subjects.collectAsState()

    var userName by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userViewModel.getUserById(userId)
        }
    }

    val user by userViewModel.users.observeAsState()
    LaunchedEffect(user) {
        user?.let { userName = it.fullName.ifEmpty { "Student" } }
    }

    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = tasks.count { !it.isCompleted }

    val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    val upcomingTasks = tasks
        .filter { !it.isCompleted }
        .sortedBy { parseDueDateForHome(it.dueDate) }
        .take(2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Welcome Header with Progress
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryGreen)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome back,",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "${userName.ifEmpty { "Student" }}!",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.flash),
                                contentDescription = "streak",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${completedTasks}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Overall Progress",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${if (tasks.isNotEmpty()) (completedTasks * 100) / tasks.size else 0}%",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = if (tasks.isNotEmpty()) completedTasks.toFloat() / tasks.size else 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color.White.copy(alpha = 0.6f),
                            trackColor = Color.White.copy(alpha = 0.2f),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$completedTasks of ${tasks.size} tasks completed",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Statistics Cards
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = subjects.size,
                        label = "Subjects",
                        icon = R.drawable.baseline_menu_book_24,
                        backgroundColor = SubjectCardGreen,
                        iconBackgroundColor = IconBackgroundGreen
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = tasks.size,
                        label = "All Tasks",
                        icon = R.drawable.baseline_add_task_24,
                        backgroundColor = TaskCardBlue,
                        iconBackgroundColor = IconBackgroundBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = completedTasks,
                        label = "Completed",
                        icon = R.drawable.baseline_check_circle_24,
                        backgroundColor = CompletedCardGreen,
                        iconBackgroundColor = IconBackgroundSuccess
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = pendingTasks,
                        label = "Pending",
                        icon = R.drawable.baseline_access_time_24,
                        backgroundColor = PendingCardOrange,
                        iconBackgroundColor = IconBackgroundOrange
                    )
                }
            }
        }

        // Upcoming Tasks Section Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Background)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_access_time_24),
                        contentDescription = null,
                        tint = DarkText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Upcoming Tasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
            }
        }

        // Upcoming Task Cards or empty state
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (upcomingTasks.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No upcoming tasks!",
                                color = GrayText,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    upcomingTasks.forEach { task ->
                        UpcomingTaskCard(
                            task = task,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }

                // Motivation Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryGreen),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.diagram),
                                contentDescription = "progress chart",
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Keep Going!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$pendingTasks tasks remaining. You've got this!",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(35.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    icon: Int,
    backgroundColor: Color,
    iconBackgroundColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(3.dp),
        modifier = modifier.height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBackgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = count.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun UpcomingTaskCard(
    task: Task,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(12.dp)
                    .background(PrimaryGreen, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkText
                )
                if (task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.note,
                        fontSize = 12.sp,
                        color = GrayText,
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_calendar_month_24),
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = task.dueDate, fontSize = 12.sp, color = GrayText)
                    if (task.subjectName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = LightGreen) {
                            Text(task.subjectName, fontSize = 11.sp, color = PrimaryGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

fun parseDueDateForHome(dateStr: String): java.util.Date {
    return try {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).parse(dateStr) ?: java.util.Date()
    } catch (e: Exception) {
        java.util.Date()
    }
}