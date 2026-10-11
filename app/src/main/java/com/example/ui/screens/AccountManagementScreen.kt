package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TokenManager
import com.example.data.model.SystemStatsDto
import com.example.data.model.UserDto
import com.example.data.repository.UserRepository
import com.example.ui.components.*
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManagementScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val userRepository = remember { UserRepository(tokenManager) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    var userList by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var stats by remember { mutableStateOf<SystemStatsDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Ban Dialog state
    var userToBan by remember { mutableStateOf<UserDto?>(null) }
    var banReason by remember { mutableStateOf("") }

    // Unban Dialog state
    var userToUnban by remember { mutableStateOf<UserDto?>(null) }

    // Delete Dialog state
    var userToDelete by remember { mutableStateOf<UserDto?>(null) }

    val myRole = tokenManager.getRole()
    val myUserId = tokenManager.getUserId()

    BackHandler {
        onNavigate(Screen.Dashboard.route)
    }

    fun loadData() {
        isLoading = true
        scope.launch {
            userRepository.getStats().onSuccess { stats = it }
            userRepository.listUsers(
                search = if (searchQuery.isBlank()) null else searchQuery.trim(),
                role = selectedRoleFilter,
                status = selectedStatusFilter
            ).onSuccess {
                userList = it.users
                isLoading = false
            }.onFailure { err ->
                isLoading = false
                Toast.makeText(context, "Gagal memuat akun: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(searchQuery, selectedRoleFilter, selectedStatusFilter) {
        loadData()
    }

    Scaffold(
        containerColor = ObsidianNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelola Akun", color = ArmorGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Panel Pengawasan $myRole", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Dashboard.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HologramCyan)
                    }
                },
                actions = {
                    IconButton(onClick = { loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = HologramCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.CreateAccount.route) },
                containerColor = ElectricBlue,
                contentColor = ObsidianNavy,
                modifier = Modifier.testTag("fab_create_user")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Create Account")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("account_management_screen")
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Stats Cards Row
            stats?.let { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Total", s.totalUsers.toString(), HologramCyan, Modifier.weight(1f))
                    StatCard("Aktif", s.activeUsers.toString(), StatusActive, Modifier.weight(1f))
                    StatCard("Banned", s.bannedUsers.toString(), StatusBanned, Modifier.weight(1f))
                    StatCard("Expired", s.expiredUsers.toString(), StatusExpired, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari username...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = HologramCyan) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_user_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HologramCyan,
                    unfocusedBorderColor = CardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatusFilter == "ACTIVE",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "ACTIVE") null else "ACTIVE" },
                    label = { Text("Active", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatusFilter == "BANNED",
                    onClick = { selectedStatusFilter = if (selectedStatusFilter == "BANNED") null else "BANNED" },
                    label = { Text("Banned", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedRoleFilter == "MEMBER",
                    onClick = { selectedRoleFilter = if (selectedRoleFilter == "MEMBER") null else "MEMBER" },
                    label = { Text("Member", fontSize = 11.sp) }
                )
                if (myRole == "OWNER") {
                    FilterChip(
                        selected = selectedRoleFilter == "RESELLER",
                        onClick = { selectedRoleFilter = if (selectedRoleFilter == "RESELLER") null else "RESELLER" },
                        label = { Text("Reseller", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = HologramCyan)
            }

            // Users List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                if (userList.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada akun ditemukan.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                items(userList) { u ->
                    val canBan = u.role != "OWNER" && u.id != myUserId && (myRole == "OWNER" || (myRole == "RESELLER" && u.created_by == myUserId))
                    UserManagementCard(
                        user = u,
                        canManage = canBan,
                        onBanClick = {
                            userToBan = u
                            banReason = ""
                        },
                        onUnbanClick = { userToUnban = u },
                        onDeleteClick = { userToDelete = u }
                    )
                }
            }
        }
    }

    // Ban Confirmation Modal
    userToBan?.let { target ->
        AlertDialog(
            onDismissRequest = { userToBan = null },
            title = { Text("Ban Akun ${target.username}", color = StatusBanned, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pengguna akan langsung kehilangan akses ke sistem.", fontSize = 12.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = banReason,
                        onValueChange = { banReason = it },
                        label = { Text("Alasan Ban") },
                        placeholder = { Text("Contoh: Pelanggaran ketentuan lisensi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = if (banReason.isBlank()) "Pelanggaran aturan" else banReason.trim()
                        scope.launch {
                            val res = userRepository.banUser(target.id, reason)
                            userToBan = null
                            res.onSuccess {
                                Toast.makeText(context, "Akun ${target.username} berhasil di-ban", Toast.LENGTH_SHORT).show()
                                loadData()
                            }.onFailure { err ->
                                Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBanned)
                ) {
                    Text("Konfirmasi Ban", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToBan = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = MidnightBlue
        )
    }

    // Unban Confirmation Modal
    userToUnban?.let { target ->
        AlertDialog(
            onDismissRequest = { userToUnban = null },
            title = { Text("Unban Akun ${target.username}", color = StatusActive, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Akses akan dipulihkan jika masa aktif akun masih berlaku. Jika sudah expired, status akun akan menjadi Expired.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = userRepository.unbanUser(target.id, "Di-unban oleh operator")
                            userToUnban = null
                            res.onSuccess {
                                Toast.makeText(context, "Akun ${target.username} berhasil di-unban", Toast.LENGTH_SHORT).show()
                                loadData()
                            }.onFailure { err ->
                                Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActive)
                ) {
                    Text("Pulihkan Akses", color = ObsidianNavy)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToUnban = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = MidnightBlue
        )
    }

    // Delete Confirmation Modal
    userToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Hapus Akun ${target.username}", color = StatusBanned, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Akun akan dihapus permanen dari basis data.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val res = userRepository.deleteUser(target.id)
                            userToDelete = null
                            res.onSuccess {
                                Toast.makeText(context, "Akun dihapus", Toast.LENGTH_SHORT).show()
                                loadData()
                            }.onFailure { err ->
                                Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBanned)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Batal", color = TextSecondary)
                }
            },
            containerColor = MidnightBlue
        )
    }
}

@Composable
private fun StatCard(title: String, count: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MidnightBlue,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun UserManagementCard(
    user: UserDto,
    canManage: Boolean,
    onBanClick: () -> Unit,
    onUnbanClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    CyberCard(
        borderColor = if (user.status == "BANNED") StatusBanned.copy(alpha = 0.5f) else CardBorder,
        backgroundColor = MidnightBlue
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoleBadge(role = user.role)
                    StatusBadge(status = user.status)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dibuat: ${user.created_at.take(10)} | Exp: ${user.expires_at?.take(10) ?: "Lifetime"}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                if (user.ban_reason != null) {
                    Text(
                        text = "Alasan Ban: ${user.ban_reason}",
                        fontSize = 10.sp,
                        color = StatusBanned
                    )
                }
            }

            if (canManage) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (user.status == "BANNED") {
                        IconButton(onClick = onUnbanClick) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Unban", tint = StatusActive)
                        }
                    } else {
                        IconButton(onClick = onBanClick) {
                            Icon(Icons.Default.Block, contentDescription = "Ban", tint = StatusBanned)
                        }
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextSecondary)
                    }
                }
            }
        }
    }
}
