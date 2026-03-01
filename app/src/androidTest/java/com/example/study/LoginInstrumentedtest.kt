package com.example.study

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<Login>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testLoginScreenUI_isVisible() {
        composeRule.onNodeWithText("Login", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Email", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Password", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Log In", ignoreCase = true).assertExists()
    }

    @Test
    fun testLoginButtonIsVisible() {
        composeRule.onNodeWithText("Log In", ignoreCase = true).assertExists()
    }

    @Test
    fun testNavigationToRegister() {
        composeRule.onNodeWithText("Sign Up", substring = true, ignoreCase = true)
            .performClick()

        intended(hasComponent(RegistrationActivity::class.java.name))
    }

    @Test
    fun testNavigationToForgetPassword() {
        composeRule.onNodeWithText("Forget Password?", ignoreCase = true)
            .performClick()

        intended(hasComponent(ForgetPasswordActivity::class.java.name))
    }
}