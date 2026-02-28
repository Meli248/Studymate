package com.example.study

import com.example.study.model.Subject
import org.junit.Assert.*
import org.junit.Test

class SubjectModelTest {

    @Test
    fun testSubjectToMap() {
        val subject = Subject(
            subjectId = "sub123",
            userId = "user456",
            name = "Science",
            createdAt = 2000L
        )

        val map = subject.toMap()

        assertEquals("sub123", map["subjectId"])
        assertEquals("user456", map["userId"])
        assertEquals("Science", map["name"])
        assertEquals(2000L, map["createdAt"])
    }

    @Test
    fun testSubjectDefaultValues() {
        val subject = Subject()
        assertEquals("", subject.subjectId)
        assertEquals("", subject.name)
        assertTrue(subject.createdAt > 0)
    }
}