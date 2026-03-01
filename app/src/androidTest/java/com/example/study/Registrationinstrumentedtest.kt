package com.example.study

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<RegistrationActivity>()

    @Test
    fun testRegisterScreenUI_isVisible() {
        composeRule.onNodeWithText("Create Your Account", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Name", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Email", ignoreCase = true).assertExists()
        composeRule.onNodeWithText("Register", ignoreCase = true).assertExists()
    }

    @Test
    fun testRegisterButtonIsVisible() {
        composeRule.onNodeWithText("Register", ignoreCase = true).assertExists()
    }

    @Test
    fun testTermsCheckboxIsVisible() {
        composeRule.onNodeWithText("I agree to the Terms & Conditions", ignoreCase = true)
            .assertExists()
    }

    @Test
    fun testLoginLinkIsVisible() {
        composeRule.onNodeWithText("Login", substring = true, ignoreCase = true)
            .performScrollTo()
            .assertExists()
    }
}