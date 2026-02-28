package com.example.study

import com.example.study.model.User
import org.junit.Assert.*
import org.junit.Test

class UserModelTest {

    @Test
    fun testUserToMap() {
        val user = User(
            id = "user123",
            fullName = "Alice Smith",
            email = "alice@example.com",
            profileImage = "image.png",
            course = "Computer Science",
            semester = "Semester 3"
        )

        val map = user.toMap()

        assertEquals("Alice Smith", map["fullName"])
        assertEquals("alice@example.com", map["email"])
        assertEquals("image.png", map["profileImage"])
        assertEquals("Computer Science", map["course"])
        assertEquals("Semester 3", map["semester"])
    }

    @Test
    fun testUserDefaultValues() {
        val user = User()
        assertEquals("", user.id)
        assertEquals("", user.fullName)
        assertEquals("", user.email)
        assertEquals("", user.profileImage)
        assertEquals("", user.course)
        assertEquals("", user.semester)
    }
}