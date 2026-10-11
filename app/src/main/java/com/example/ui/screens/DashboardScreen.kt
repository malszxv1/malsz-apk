package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userRepository = remember { UserRepository(tokenManager) }
    val authRepository = remember { AuthRepository(tokenManager) }
    val scope = rememberCoroutineScope()

    var userProfile by remember { mutableStateOf<UserDto?>(null) }
    var isServerOnline by remember { mutableStateOf<Boolean?>(null) }
    var isFloatingAIActive by remember { mutableStateOf(false) }
    var isCheckingPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fetch fresh profile & check server health
        launch {
            val healthRes = authRepository.checkServerHealth()
            isServerOnline = healthRes.isSuccess && healthRes.getOrNull() == true
        }
        launch {
            val profileRes = userRepository.getProfile()
            profileRes.onSuccess { userProfile = it }
        }
    }

    val username = userProfile?.username ?: tokenManager.getUsername() ?: "User"
    val role = userProfile?.role ?: tokenManager.getRole()
    val status = userProfile?.status ?: tokenManager.getStatus()
    val expiresAt = userProfile?.expires_at ?: tokenManager.getExpiresAt()

    val canManageAccounts = role in listOf("OWNER", "ADMIN", "RESELLER")

    Scaffold(
        containerColor = ObsidianNavy,
        bottomBar = {
            NavigationBar(
                containerColor = MidnightBlue,
                contentColor = TextPrimary
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Already here */ },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HologramCyan,
                        selectedTextColor = HologramCyan,
                        indicatorColor = SurfaceElevated
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
                    selected = false,
                    onClick = { onNavigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("dashboard_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Brand Header: "Welcome to Malsz Project" (Requirement 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Welcome to Malsz Project",
                        color = ArmorGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CYBER COMPANION",
                        color = HologramCyan,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Server Status Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MidnightBlue,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isServerOnline == true) StatusActive.copy(alpha = 0.5f) else StatusBanned.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isServerOnline == true) StatusActive else StatusBanned)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isServerOnline == true) "SERVER ONLINE" else "SERVER OFFLINE",
                            color = if (isServerOnline == true) StatusActive else StatusBanned,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // User Identity Card
            CyberCard(
                borderColor = CardBorderCyan,
                backgroundColor = MidnightBlue
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RobotAvatar(size = 72.dp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = username,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RoleBadge(role = role)
                            StatusBadge(status = status)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (expiresAt != null) "Kedaluwarsa: ${expiresAt.take(10)}" else "Masa Aktif: LIFETIME (Permanen)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Featured Core Card: Floating AI Assistant (Requirement 3 & 8)
            CyberCard(
                borderColor = HologramCyan,
                backgroundColor = SurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FITUR UTAMA",
                            color = ArmorGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Floating AI Robot",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HologramCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Robot mengambang pintar di atas aplikasi lain: Isi Otomatis, Cari Jawaban (Screenshot), dan Tanya AI.",
                            fontSize = 11.sp,
                            color = RobotSilver
                        )
                    }
                    Switch(
                        checked = isFloatingAIActive,
                        onCheckedChange = { enable ->
                            if (enable) {
                                // Check Overlay Permission
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                    Toast.makeText(
                                        context,
                                        "Izinkan 'Tampilkan di atas aplikasi lain' untuk Floating AI",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                    return@Switch
                                }
                                FloatingAIService.startService(context)
                                isFloatingAIActive = true
                                Toast.makeText(context, "Floating AI Robot diaktifkan di layar!", Toast.LENGTH_SHORT).show()
                            } else {
                                FloatingAIService.stopService(context)
                                isFloatingAIActive = false
                                Toast.makeText(context, "Floating AI Robot dinonaktifkan", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HologramCyan,
                            checkedTrackColor = ElectricBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MidnightBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Aksesibilitas", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (AIAccessibilityService.isRunning()) "Aktif ✅" else "Nonaktif ⚠️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (AIAccessibilityService.isRunning()) StatusActive else StatusExpired
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MidnightBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Overlay Display", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) "Diizinkan ✅" else "Belum Diizinkan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) StatusActive else StatusExpired
                            )
                        }
                    }
                }
            }

            // Quick Access Panels
            if (canManageAccounts) {
                CyberCard(
                    borderColor = CardBorderGold,
                    backgroundColor = MidnightBlue
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PANEL $role",
                                color = ArmorGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kelola Akun & Lisensi",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { onNavigate(Screen.AccountManagement.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = ArmorGold)
                        ) {
                            Text("Buka Panel", color = ObsidianNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Recent Activity Section
            CyberCard(
                borderColor = CardBorder,
                backgroundColor = MidnightBlue
            ) {
                Text(
                    text = "Aktivitas Terkini",
                    color = HologramCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                ActivityItem(
                    title = "Autentikasi Berhasil",
                    desc = "Sesi login terverifikasi aman melalui token server.",
                    time = "Baru saja"
                )
                ActivityItem(
                    title = "Floating AI Engine Siap",
                    desc = "Modul multimodal Gemini & injeksi teks siap dijalankan.",
                    time = "1m lalu"
                )
                ActivityItem(
                    title = "Koneksi Jaringan Cyber",
                    desc = "Sinkronisasi real-time ke database SQLite backend aktif.",
                    time = "5m lalu"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ActivityItem(title: String, desc: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(HologramCyan)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = TextSecondary, fontSize = 10.sp)
        }
        Text(text = time, color = TextMuted, fontSize = 10.sp)
    }
}
