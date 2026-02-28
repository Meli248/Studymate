package com.example.study

import com.example.study.model.Task
import org.junit.Assert.*
import org.junit.Test

class TaskModelTest {

    @Test
    fun testTaskToMap() {
        val task = Task(
            taskId = "task123",
            userId = "user456",
            subjectId = "sub789",
            subjectName = "Math",
            title = "Homework",
            note = "Chapter 1",
            dueDate = "Oct 25, 2023",
            priority = "High",
            isCompleted = true,
            createdAt = 1000L
        )

        val map = task.toMap()

        assertEquals("task123", map["taskId"])
        assertEquals("user456", map["userId"])
        assertEquals("sub789", map["subjectId"])
        assertEquals("Math", map["subjectName"])
        assertEquals("Homework", map["title"])
        assertEquals("Chapter 1", map["note"])
        assertEquals("Oct 25, 2023", map["dueDate"])
        assertEquals("High", map["priority"])
        assertEquals(true, map["completed"]) // Database field name is "completed"
        assertEquals(1000L, map["createdAt"])
    }

    @Test
    fun testTaskDefaultValues() {
        val task = Task()
        assertEquals("", task.taskId)
        assertEquals("", task.userId)
        assertFalse(task.isCompleted)
        assertEquals("Imp", task.priority)
        assertTrue(task.createdAt > 0)
    }
}