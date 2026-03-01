package com.example.study

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RegistrationActivity>()

    @Before
    fun setup() {
        FirebaseAuth.getInstance().signOut()
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testRegistrationSuccess() {
        val testName = "Test User"
        // Unique email for every run
        val testEmail = "test_${System.currentTimeMillis()}@example.com"
        val testPassword = "password123"

        // Fill in details
        composeRule.onNodeWithText("Enter your full name").performTextInput(testName)
        composeRule.onNodeWithText("Enter your email address").performTextInput(testEmail)
        composeRule.onNodeWithText("Enter your password").performTextInput(testPassword)

        // Use isToggleable() to uniquely identify the checkbox
        composeRule.onNode(isToggleable()).performClick()

        // Click Register
        composeRule.onNodeWithText("Register", ignoreCase = true).performClick()

        // Wait for navigation to DashboardActivity
        // Changed catch to Throwable because intended throws AssertionError
        composeRule.waitUntil(20000) {
            try {
                intended(hasComponent(DashboardActivity::class.java.name))
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    @Test
    fun testNavigationToLogin() {
        composeRule.onNodeWithText("Login", substring = true, ignoreCase = true)
            .performClick()
        intended(hasComponent(Login::class.java.name))
    }
}
