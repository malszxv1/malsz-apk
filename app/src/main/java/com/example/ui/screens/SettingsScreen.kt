package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ApiClient
import com.example.data.local.TokenManager
import com.example.data.model.UserDto
import com.example.data.repository.AuthRepository
import com.example.data.repository.UserRepository
import com.example.service.AIAccessibilityService
import com.example.service.FloatingAIService
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val authRepository = remember { AuthRepository(tokenManager) }
    val userRepository = remember { UserRepository(tokenManager) }
    val scope = rememberCoroutineScope()

    var userProfile by remember { mutableStateOf<UserDto?>(null) }

    // Change Password states
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isChangingPass by remember { mutableStateOf(false) }

    // Delete Account states
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }

    // Server Config states
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(tokenManager.getServerBaseUrl()) }

    BackHandler {
        onNavigate(Screen.Dashboard.route)
    }

    LaunchedEffect(Unit) {
        val res = userRepository.getProfile()
        res.onSuccess { userProfile = it }
    }

    Scaffold(
        containerColor = ObsidianNavy,
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Profil", color = HologramCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Dashboard.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HologramCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MidnightBlue,
                contentColor = TextPrimary
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(Screen.Dashboard.route) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigate(Screen.GlobalChat.route) },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Global Chat") },
                    label = { Text("Chat Global") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Current */ },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HologramCyan,
                        selectedTextColor = HologramCyan,
                        indicatorColor = SurfaceElevated
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Profil Section
            CyberCard(
                borderColor = CardBorderCyan,
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "1. PROFIL PENGGUNA",
                    color = HologramCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RobotAvatar(size = 64.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = userProfile?.username ?: tokenManager.getUsername() ?: "User",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RoleBadge(role = userProfile?.role ?: tokenManager.getRole())
                            StatusBadge(status = userProfile?.status ?: tokenManager.getStatus())
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tanggal Pembuatan: ${userProfile?.created_at?.take(10) ?: "-"}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = if (userProfile?.expires_at != null) "Kedaluwarsa: ${userProfile?.expires_at?.take(10)}" else "Masa Aktif: Lifetime / Permanen",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // 2. Change Password Section
            CyberCard(
                borderColor = CardBorder,
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "2. GANTI PASSWORD",
                    color = HologramCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Password Lama") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Password Baru (min 6 karakter)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Konfirmasi Password Baru") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                CyberButton(
                    text = "Perbarui Password",
                    onClick = {
                        if (oldPassword.isBlank() || newPassword.isBlank()) {
                            Toast.makeText(context, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                            return@CyberButton
                        }
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                            return@CyberButton
                        }
                        isChangingPass = true
                        scope.launch {
                            val res = authRepository.changePassword(oldPassword, newPassword)
                            isChangingPass = false
                            res.onSuccess {
                                Toast.makeText(context, "Password berhasil diubah. Silakan login kembali.", Toast.LENGTH_LONG).show()
                                onLogout()
                            }.onFailure { err ->
                                Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    isLoading = isChangingPass,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. Pengaturan Floating AI
            CyberCard(
                borderColor = CardBorder,
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "3. PENGATURAN FLOATING AI",
                    color = HologramCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Overlay Permission Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Izin Tampilkan di Atas Aplikasi (Overlay)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) "Status: Diberikan ✅" else "Status: Belum Diizinkan ⚠️",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Atur Izin", color = ObsidianNavy, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Accessibility Service Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Layanan Aksesibilitas (Isi Otomatis)", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (AIAccessibilityService.isRunning()) "Status: Aktif ✅" else "Status: Belum Aktif ⚠️",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Buka Aksesibilitas", color = ObsidianNavy, fontSize = 11.sp)
                    }
                }
            }

            // 4. Server URL & Connection Configuration
            CyberCard(
                borderColor = CardBorder,
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "4. KONEKSI SERVER BACKEND",
                    color = HologramCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Server saat ini: ${tokenManager.getServerBaseUrl()}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showServerDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ubah Alamat Server API", color = HologramCyan)
                }
            }

            // 5. Delete Account & Logout
            CyberCard(
                borderColor = StatusBanned.copy(alpha = 0.5f),
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "5. TINDAKAN AKUN SENSITIF",
                    color = StatusBanned,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                CyberButton(
                    text = "LOGOUT DARI APLIKASI",
                    onClick = {
                        scope.launch {
                            authRepository.logout()
                            onLogout()
                        }
                    },
                    color = ElectricBlue,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusBanned)
                ) {
                    Text("Hapus Akun Saya Secara Permanen", color = StatusBanned)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Konfirmasi Hapus Akun", color = StatusBanned, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus akun Anda? Seluruh riwayat dan akses lisensi akan dicabut permanen.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeletingAccount = true
                        scope.launch {
                            val res = userRepository.deleteMyAccount()
                            isDeletingAccount = false
                            showDeleteAccountDialog = false
                            res.onSuccess {
                                Toast.makeText(context, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()
                                onLogout()
                            }.onFailure { err ->
                                Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBanned)
                ) {
                    Text("Ya, Hapus Sekarang", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = MidnightBlue
        )
    }

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text("Ubah Base URL Server", color = HologramCyan) },
            text = {
                OutlinedTextField(
                    value = serverUrlInput,
                    onValueChange = { serverUrlInput = it },
                    label = { Text("Server URL (misal: http://10.0.2.2:5000/)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        tokenManager.setServerBaseUrl(serverUrlInput.trim())
                        ApiClient.invalidate()
                        showServerDialog = false
                        Toast.makeText(context, "Alamat server diperbarui", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Simpan", color = ObsidianNavy)
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = MidnightBlue
        )
    }
}
