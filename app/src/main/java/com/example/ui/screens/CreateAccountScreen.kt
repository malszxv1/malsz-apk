package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TokenManager
import com.example.data.repository.UserRepository
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.RoleBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userRepository = remember { UserRepository(tokenManager) }
    val scope = rememberCoroutineScope()

    val myRole = tokenManager.getRole()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(if (myRole == "RESELLER") "MEMBER" else "MEMBER") }
    var selectedExpiration by remember { mutableStateOf("30") } // days
    var isCreating by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    val roleOptions = if (myRole == "OWNER") listOf("ADMIN", "RESELLER", "MEMBER") else listOf("MEMBER")
    val expirationOptions = listOf(
        Pair("1 Hari", "1"),
        Pair("3 Hari", "3"),
        Pair("7 Hari", "7"),
        Pair("30 Hari", "30"),
        Pair("90 Hari", "90"),
        Pair("365 Hari", "365"),
        Pair("Lifetime", "LIFETIME")
    )

    BackHandler {
        onNavigate(Screen.AccountManagement.route)
    }

    Scaffold(
        containerColor = ObsidianNavy,
        topBar = {
            TopAppBar(
                title = { Text("Buat Akun Baru", color = HologramCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.AccountManagement.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HologramCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("create_account_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            CyberCard(
                borderColor = CardBorderCyan,
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "FORMULIR PENDAFTARAN LISENSI",
                    color = ArmorGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        resultMessage = null
                    },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_username_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        resultMessage = null
                    },
                    label = { Text("Password Awal") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_password_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm Password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        resultMessage = null
                    },
                    label = { Text("Konfirmasi Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_confirm_password_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Selection
                Text("Pilih Role Akun:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    roleOptions.forEach { r ->
                        val isSelected = selectedRole == r
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ElectricBlue else SurfaceElevated,
                            onClick = {
                                if (myRole == "OWNER") {
                                    selectedRole = r
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = r,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ObsidianNavy else TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Expiration Selection
                Text("Pilih Masa Aktif Akun:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val chunked = expirationOptions.chunked(3)
                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { (label, value) ->
                                val isSelected = selectedExpiration == value
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) HologramCyan else SurfaceElevated,
                                    onClick = { selectedExpiration = value },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) ObsidianNavy else RobotSilver
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (resultMessage != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = resultMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (resultMessage!!.startsWith("✅")) StatusActive else StatusBanned
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                CyberButton(
                    text = "BUAT AKUN SEKARANG",
                    onClick = {
                        if (username.isBlank() || password.isBlank()) {
                            resultMessage = "Username dan password harus diisi."
                            return@CyberButton
                        }
                        if (password != confirmPassword) {
                            resultMessage = "Konfirmasi password tidak cocok."
                            return@CyberButton
                        }

                        isCreating = true
                        resultMessage = null
                        scope.launch {
                            val res = userRepository.createUser(
                                username = username.trim(),
                                password = password,
                                role = selectedRole,
                                expirationDays = selectedExpiration
                            )
                            isCreating = false
                            res.onSuccess { created ->
                                resultMessage = "✅ Akun '${created.username}' (${created.role}) berhasil dibuat! Masa aktif: ${created.expires_at?.take(10) ?: "Lifetime"}"
                                username = ""
                                password = ""
                                confirmPassword = ""
                                Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                            }.onFailure { err ->
                                resultMessage = "Gagal: ${err.message}"
                            }
                        }
                    },
                    isLoading = isCreating,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_create_account"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
