package com.example.study

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.study.ui.theme.StudyTheme
import com.example.study.viewmodel.SubjectViewModel
import com.example.study.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : ComponentActivity() {

    // Shared ViewModels — single instance for the whole activity
    private val taskViewModel: TaskViewModel by viewModels { TaskViewModel.Factory }
    private val subjectViewModel: SubjectViewModel by viewModels { SubjectViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start loading data once
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            taskViewModel.loadTasks(userId)
            subjectViewModel.loadSubjects(userId)
        }

        enableEdgeToEdge()
        setContent {
            StudyTheme {
                DashboardBody(taskViewModel, subjectViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody(
    taskViewModel: TaskViewModel,
    subjectViewModel: SubjectViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity

    data class NavItem(val label: String, val icon: Int)
    var selectedIndex by remember { mutableStateOf(0) }

    val navList = listOf(
        NavItem("Home", R.drawable.baseline_home_24),
        NavItem("Subject", R.drawable.baseline_subject_24),
        NavItem("Task", R.drawable.baseline_add_task_24),
        NavItem("Profile", R.drawable.baseline_person_24)
    )

    Scaffold(
        topBar = {
            if (selectedIndex != 0) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedIndex) {
                                1 -> "Subjects"
                                2 -> "Tasks"
                                3 -> "Profile"
                                else -> "Study App"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = DarkText
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = PrimaryGreen
            ) {
                navList.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = null
                            )
                        },
                        label = { Text(item.label, fontSize = 12.sp) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryGreen,
                            selectedTextColor = PrimaryGreen,
                            indicatorColor = LightGreen,
                            unselectedIconColor = GrayText,
                            unselectedTextColor = GrayText
                        )
                    )
                }
            }
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(taskViewModel, subjectViewModel)
                1 -> Subject(subjectViewModel, taskViewModel)
                2 -> Task(taskViewModel, subjectViewModel)
                3 -> Profile()
                else -> HomeScreen(taskViewModel, subjectViewModel)
            }
        }
    }
}