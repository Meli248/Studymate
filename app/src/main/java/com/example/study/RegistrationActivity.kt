package com.example.study

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.study.model.User
import com.example.study.repository.userRepoImpl
import com.example.study.ui.theme.StudyTheme
import com.example.study.viewmodel.UserViewModel

val FieldGray = Color(0xFFF2F5F4)
val PrimaryGreen = Color(0xFF5E8B7E)
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

val LightGreen = Color(0xFFE7F2EE)
val DarkText = Color(0xFF2E3A3A)
val GrayText = Color(0xFF8A9A9A)
val PendingRed = Color(0xFF7C2929)
val Background = Color(0xFFF9FBFA)

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyTheme {
                RegistrationBody()
            }
        }
    }
}

@Composable
fun RegistrationBody() {

    val userViewModel = remember { UserViewModel(userRepoImpl()) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val sharedPreference = context.getSharedPreferences("User", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {

        Image(
            painter = painterResource(id = R.drawable.f),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 5.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Your Account",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Create your account to start your journey",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Name field
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                Text("Name", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = { Text("Enter your full name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FieldGray,
                        unfocusedContainerColor = FieldGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email field
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                Text("Email", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email address") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
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
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                Text("Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(5.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible)
                                        R.drawable.baseline_visibility_24
                                    else
                                        R.drawable.baseline_visibility_off_24
                                ),
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = FieldGray,
                        unfocusedContainerColor = FieldGray,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Terms
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen)
                )
                Text(
                    text = "I agree to the Terms & Conditions",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (!termsAccepted) {
                        Toast.makeText(context, "Please agree to Terms & Conditions", Toast.LENGTH_SHORT).show()
                    } else {
                        val localEmail: String? = sharedPreference.getString("email", "")
                        if (localEmail == email) {
                            Toast.makeText(context, "email already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.register(fullName, email, password) { success, msg, userId ->
                                if (success) {
                                    val model = User(
                                        id = userId,
                                        email = email,
                                        fullName = fullName
                                    )
                                    userViewModel.addUserToDatabase(userId, model) { success2, msg2 ->
                                        if (success2) {
                                            Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()
                                            val intent = Intent(context, DashboardActivity::class.java)
                                            context.startActivity(intent)
                                            activity?.finish()
                                        } else {
                                            Toast.makeText(context, msg2, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            ) {
                Text(text = "Register", color = Color.White, fontSize = 17.sp)
            }


            Spacer(modifier = Modifier.height(14.dp))

            Text(
                buildAnnotatedString {
                    append("Don't have an account? ")
                    withStyle(SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Medium)) {
                        append("Login")
                    }
                },
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        val intent = Intent(context, Login::class.java)
                        context.startActivity(intent)
                        activity?.finish()
                    }
            )

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Preview
@Composable
fun PreviewRegistration() {
    RegistrationBody()
}
