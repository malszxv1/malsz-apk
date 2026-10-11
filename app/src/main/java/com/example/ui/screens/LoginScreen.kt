package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiClient
import com.example.data.local.TokenManager
import com.example.data.repository.AuthRepository
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.RobotAvatar
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val authRepository = remember { AuthRepository(tokenManager) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(tokenManager.getServerBaseUrl()) }

    fun executeLogin() {
        focusManager.clearFocus()
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Harap masukkan username dan password."
            return
        }

        isLoading = true
        errorMessage = null
        scope.launch {
            val result = authRepository.login(username.trim(), password)
            result.onSuccess {
                isLoading = false
                onLoginSuccess()
            }.onFailure { error ->
                isLoading = false
                errorMessage = error.message ?: "Koneksi ke server gagal. Pastikan server aktif."
            }
        }
    }

    Scaffold(
        containerColor = ObsidianNavy
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .testTag("login_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RobotAvatar(size = 90.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MALSZ CYBER ACCESS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HologramCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Autentikasi Akun Resmi Malsz AI Robot",
                    fontSize = 12.sp,
                    color = RobotSilver.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                CyberCard(
                    borderColor = CardBorderCyan,
                    backgroundColor = MidnightBlue
                ) {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Username Icon", tint = HologramCyan)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HologramCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = HologramCyan,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password Icon", tint = HologramCyan)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { executeLogin() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HologramCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = HologramCyan,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage!!,
                            color = StatusBanned,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("error_message")
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    CyberButton(
                        text = "LOGIN KE SISTEM",
                        onClick = { executeLogin() },
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "login_button"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Server URL Config & Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showServerConfigDialog = true },
                        modifier = Modifier.testTag("server_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Server Setting",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Konfigurasi Server (${tokenManager.getServerBaseUrl()})",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }

    if (showServerConfigDialog) {
        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = { Text("Pengaturan Alamat Server", color = HologramCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Emulator Android mengakses server lokal via 10.0.2.2:5000",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Base URL Server") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tokenManager.setServerBaseUrl(serverUrlInput.trim())
                        ApiClient.invalidate()
                        showServerConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Simpan", color = ObsidianNavy)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = MidnightBlue
        )
    }
}
