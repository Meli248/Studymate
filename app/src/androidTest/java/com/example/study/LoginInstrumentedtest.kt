package com.example.study

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<Login>

    @Before
    fun setup() {
        FirebaseAuth.getInstance().signOut()
        Intents.init()
        scenario = ActivityScenario.launch(Login::class.java)
    }

    @After
    fun tearDown() {
        Intents.release()
        if (this::scenario.isInitialized) {
            scenario.close()
        }
    }

    @Test
    fun testLoginProcess_withNewUser() {
        val testEmail = "login_test_${System.currentTimeMillis()}@example.com"
        val testPassword = "password123"
        val testName = "Login Test User"

        // 1. Navigate to Registration to ensure we have a valid user
        composeRule.onNodeWithText("Sign Up", substring = true, ignoreCase = true).performClick()
        
        // 2. Register the user
        composeRule.onNodeWithText("Enter your full name").performTextInput(testName)
        composeRule.onNodeWithText("Enter your email address").performTextInput(testEmail)
        composeRule.onNodeWithText("Enter your password").performTextInput(testPassword)
        composeRule.onNode(isToggleable()).performClick()
        composeRule.onNodeWithText("Register", ignoreCase = true).performClick()

        // 3. Wait for Dashboard to confirm registration worked
        composeRule.waitUntil(15000) {
            try {
                intended(hasComponent(DashboardActivity::class.java.name))
                true
            } catch (e: Throwable) {
                false
            }
        }

        // 4. Log out and return to Login screen for the actual Login test
        FirebaseAuth.getInstance().signOut()
        scenario.close()
        scenario = ActivityScenario.launch(Login::class.java)

        // 5. Perform the Login test
        composeRule.onNodeWithText("Enter your email address").performTextInput(testEmail)
        composeRule.onNodeWithText("Enter your password").performTextInput(testPassword)
        composeRule.onNodeWithText("Log In", ignoreCase = true).performClick()

        // 6. Final verification
        composeRule.waitUntil(15000) {
            try {
                intended(hasComponent(DashboardActivity::class.java.name))
                true
            } catch (e: Throwable) {
                false
            }
        }
    }

    @Test
    fun testNavigationToRegister() {
        composeRule.onNodeWithText("Sign Up", substring = true, ignoreCase = true)
            .performClick()
        intended(hasComponent(RegistrationActivity::class.java.name))
    }
}
