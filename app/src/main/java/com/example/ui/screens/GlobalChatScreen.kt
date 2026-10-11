package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ChatMessageDto
import com.example.data.repository.ChatRepository
import com.example.ui.components.RoleBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalChatScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val chatRepository = remember { ChatRepository(tokenManager) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<ChatMessageDto>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var selectedMessageForAction by remember { mutableStateOf<ChatMessageDto?>(null) }

    val myUserId = tokenManager.getUserId()
    val myRole = tokenManager.getRole()

    BackHandler {
        onNavigate(Screen.Dashboard.route)
    }

    fun loadMessages() {
        scope.launch {
            val result = chatRepository.listMessages(limit = 60)
            result.onSuccess {
                messages = it
                if (it.isNotEmpty()) {
                    listState.animateScrollToItem(it.size - 1)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMessages()
        // Periodically poll for new messages
        while (true) {
            delay(5000)
            val res = chatRepository.listMessages(limit = 60)
            res.onSuccess { messages = it }
        }
    }

    Scaffold(
        containerColor = ObsidianNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chat Global", color = HologramCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Ruang komunikasi real-time Malsz AI Robot", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Screen.Dashboard.route) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HologramCyan)
                    }
                },
                actions = {
                    IconButton(onClick = { loadMessages() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = HologramCyan)
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
                    selected = true,
                    onClick = { /* Current */ },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Global Chat") },
                    label = { Text("Chat Global") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = HologramCyan,
                        selectedTextColor = HologramCyan,
                        indicatorColor = SurfaceElevated
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
                .imePadding()
                .testTag("global_chat_screen")
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada pesan. Jadilah yang pertama menyapa!",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(messages) { msg ->
                    val isMe = msg.user_id == myUserId
                    MessageBubble(
                        message = msg,
                        isMe = isMe,
                        onClick = { selectedMessageForAction = msg }
                    )
                }
            }

            // Message Input Bar
            Surface(
                color = MidnightBlue,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Tulis pesan global...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HologramCyan,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isSending) {
                                val text = inputText.trim()
                                inputText = ""
                                isSending = true
                                scope.launch {
                                    val res = chatRepository.sendMessage(text)
                                    isSending = false
                                    res.onSuccess {
                                        loadMessages()
                                    }.onFailure { err ->
                                        Toast.makeText(context, "Gagal: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ElectricBlue)
                            .testTag("send_chat_button"),
                        enabled = !isSending
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = ObsidianNavy
                        )
                    }
                }
            }
        }
    }

    // Moderation Action Dialog
    selectedMessageForAction?.let { msg ->
        val canDelete = myRole in listOf("OWNER", "ADMIN") || msg.user_id == myUserId
        AlertDialog(
            onDismissRequest = { selectedMessageForAction = null },
            title = { Text("Opsi Pesan", color = HologramCyan, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pengirim: ${msg.username} (${msg.role})", fontSize = 12.sp, color = TextPrimary)
                    Text("Pesan: \"${msg.message}\"", fontSize = 12.sp, color = RobotSilver)
                }
            },
            confirmButton = {
                if (canDelete) {
                    Button(
                        onClick = {
                            scope.launch {
                                chatRepository.deleteMessage(msg.id)
                                loadMessages()
                                selectedMessageForAction = null
                                Toast.makeText(context, "Pesan dihapus", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusBanned)
                    ) {
                        Text("Hapus Pesan", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            chatRepository.reportMessage(msg.id, "Spam / inappropriate")
                            selectedMessageForAction = null
                            Toast.makeText(context, "Pesan telah dilaporkan ke moderator", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Laporkan", color = ArmorGold)
                }
            },
            containerColor = MidnightBlue
        )
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessageDto,
    isMe: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isMe) "Anda" else message.username,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMe) HologramCyan else ArmorGold
            )
            RoleBadge(role = message.role)
            Text(
                text = message.created_at.take(16).replace("T", " "),
                fontSize = 9.sp,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isMe) 14.dp else 2.dp,
                bottomEnd = if (isMe) 2.dp else 14.dp
            ),
            color = if (isMe) ElectricBlue.copy(alpha = 0.25f) else SurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isMe) ElectricBlue else CardBorder
            ),
            onClick = onClick,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                text = message.message,
                color = TextPrimary,
                fontSize = 13.sp,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}
