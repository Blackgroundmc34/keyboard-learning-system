package com.privatecloset.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecloset.app.data.model.UserRole
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.ui.common.BrandWordmark
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.BgStroke
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.GoldLight
import com.privatecloset.app.ui.theme.Purple
import com.privatecloset.app.ui.theme.PurpleLight
import com.privatecloset.app.ui.theme.ScreenBackgroundGradient
import com.privatecloset.app.ui.theme.TextPrimary
import com.privatecloset.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    repository: LearningRepository? = null,
    onGetStarted: () -> Unit,
    onLogIn: () -> Unit,
) {
    var showAuthModal by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }

    var emailInput by remember { mutableStateOf("student@example.com") }
    var passwordInput by remember { mutableStateOf("password123") }
    var nameInput by remember { mutableStateOf("Mthuthuzeli Ndwendwa") }
    var usernameInput by remember { mutableStateOf("mthuthuzeli") }
    var inviteCodeInput by remember { mutableStateOf("MASTER-TUTOR-2026") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundGradient)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandWordmark(markSize = 120.dp)

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                isRegisterMode = true
                selectedRole = UserRole.STUDENT
                showAuthModal = true
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleLight),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Student Get Started", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        OutlinedButton(
            onClick = {
                isRegisterMode = false
                selectedRole = UserRole.STUDENT
                showAuthModal = true
            },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Gold),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text("Student Log In", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(14.dp))

        // Dedicated Tutor / Instructor Entrance
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BgCard)
                .border(1.dp, BgStroke, RoundedCornerShape(12.dp))
                .clickable {
                    isRegisterMode = false
                    selectedRole = UserRole.INSTRUCTOR
                    emailInput = "tutor@privatecloset.com"
                    showAuthModal = true
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.School, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
            Text("Are you an Instructor / Tutor? Sign In Here", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Connects directly with backend (/api/auth)",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    if (showAuthModal) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showAuthModal = false },
            containerColor = BgCard,
            title = {
                Column {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Account Sign In",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    // Role Selector Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF130E1F))
                            .padding(2.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedRole == UserRole.STUDENT) Purple else Color.Transparent)
                                .clickable {
                                    selectedRole = UserRole.STUDENT
                                    if (emailInput.contains("tutor")) emailInput = "student@example.com"
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Student", color = if (selectedRole == UserRole.STUDENT) Color.White else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedRole == UserRole.INSTRUCTOR) Gold else Color.Transparent)
                                .clickable {
                                    selectedRole = UserRole.INSTRUCTOR
                                    if (emailInput.contains("student")) emailInput = "tutor@privatecloset.com"
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Tutor / Instructor", color = if (selectedRole == UserRole.INSTRUCTOR) Color.Black else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (selectedRole == UserRole.INSTRUCTOR) {
                            if (isRegisterMode) "Instructor registration requires a Master Invite Code." else "Instructor portal login with course upload access."
                        } else {
                            if (isRegisterMode) "Register your student profile." else "Enter student credentials to sync progress."
                        },
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = GoldLight) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = BgStroke,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = GoldLight) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = BgStroke,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (selectedRole == UserRole.INSTRUCTOR) {
                            OutlinedTextField(
                                value = inviteCodeInput,
                                onValueChange = { inviteCodeInput = it },
                                label = { Text("Master Tutor Invite Code") },
                                leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = Gold) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = BgStroke,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null, tint = GoldLight) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = BgStroke,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = GoldLight) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = BgStroke,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (repository == null) {
                            showAuthModal = false
                            if (isRegisterMode) onGetStarted() else onLogIn()
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = if (isRegisterMode) {
                                if (selectedRole == UserRole.INSTRUCTOR) {
                                    repository.registerTutor(
                                        username = usernameInput,
                                        email = emailInput,
                                        password = passwordInput,
                                        fullName = nameInput,
                                        inviteCode = inviteCodeInput,
                                    )
                                } else {
                                    repository.register(
                                        username = usernameInput,
                                        email = emailInput,
                                        password = passwordInput,
                                        fullName = nameInput,
                                    )
                                }
                            } else {
                                val logRes = repository.login(email = emailInput, password = passwordInput)
                                if (logRes.isSuccess && selectedRole == UserRole.INSTRUCTOR) {
                                    repository.setUserRole(UserRole.INSTRUCTOR)
                                }
                                logRes
                            }
                            isLoading = false
                            if (result.isSuccess) {
                                showAuthModal = false
                                if (isRegisterMode) onGetStarted() else onLogIn()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Authentication failed"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (isRegisterMode) "Register" else "Log In", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                        Text(if (isRegisterMode) "Switch to Log In" else "Create Account", color = GoldLight)
                    }
                    TextButton(onClick = { showAuthModal = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            }
        )
    }
}
