package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ChatHistoryItem
import com.example.data.repository.AIRepository
import com.example.service.AIAccessibilityService
import com.example.service.ScreenCaptureManager
import com.example.ui.components.CyberButton
import com.example.ui.components.RobotAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class FloatingTab {
    MENU,
    AUTO_FILL,
    SOLVE_SCREEN,
    ASK_AI
}

@Composable
fun FloatingAIPanel(
    onClose: () -> Unit,
    onHideToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val aiRepository = remember { AIRepository(tokenManager) }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(FloatingTab.MENU) }
    var isHiddenMode by remember { mutableStateOf(false) }

    // Auto Fill States
    var autoFillStatus by remember { mutableStateOf("Siap menganalisis kolom teks") }
    var autoFillPreviewText by remember { mutableStateOf("") }
    var autoFillCountdown by remember { mutableStateOf(0) }
    var isAutoFilling by remember { mutableStateOf(false) }

    // Solve Screen States
    var solveStatusText by remember { mutableStateOf("Siap menganalisis soal layar") }
    var isSolvingScreen by remember { mutableStateOf(false) }
    var solvedAnswer by remember { mutableStateOf("") }
    var solvedReason by remember { mutableStateOf("") }
    var solvedQuestion by remember { mutableStateOf("") }

    // Ask AI States
    var askInput by remember { mutableStateOf("") }
    var isAskingAI by remember { mutableStateOf(false) }
    val chatHistory = remember { mutableStateListOf<Pair<String, Boolean>>() } // text, isUser

    fun copyToClipboard(text: String, label: String = "Malsz AI") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Tersalin ke clipboard!", Toast.LENGTH_SHORT).show()
    }

    Card(
        modifier = Modifier
            .width(320.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, HologramCyan, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = ObsidianNavy.copy(alpha = 0.95f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RobotAvatar(size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "MALSZ CYBER AI",
                            fontWeight = FontWeight.Bold,
                            color = HologramCyan,
                            fontSize = 13.sp
                        )
                        Text(
                            text = when (currentTab) {
                                FloatingTab.MENU -> "Panel Kontrol Utama"
                                FloatingTab.AUTO_FILL -> "1. Isi Otomatis"
                                FloatingTab.SOLVE_SCREEN -> "2. Cari Jawaban"
                                FloatingTab.ASK_AI -> "3. Tanya AI"
                            },
                            color = RobotSilver,
                            fontSize = 10.sp
                        )
                    }
                }

                Row {
                    if (currentTab != FloatingTab.MENU) {
                        IconButton(
                            onClick = { currentTab = FloatingTab.MENU },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = HologramCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = RobotSilver,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = CardBorder
            )

            // Dynamic Content
            when (currentTab) {
                FloatingTab.MENU -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MenuItemRow(
                            icon = Icons.Default.Edit,
                            title = "1. Isi Otomatis",
                            desc = "Bantu isi jawaban pada kolom aktif (jeda 2 detik)",
                            onClick = { currentTab = FloatingTab.AUTO_FILL }
                        )

                        MenuItemRow(
                            icon = Icons.Default.CameraAlt,
                            title = "2. Cari Jawaban",
                            desc = "Screenshot AI & analisis soal layar instan",
                            onClick = { currentTab = FloatingTab.SOLVE_SCREEN }
                        )

                        MenuItemRow(
                            icon = Icons.Default.Chat,
                            title = "3. Tanya AI",
                            desc = "Percakapan cepat tanpa keluar aplikasi",
                            onClick = { currentTab = FloatingTab.ASK_AI }
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MidnightBlue)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "4. Sembunyikan AI",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isHiddenMode) "Robot disembunyikan" else "Robot tampil aktif",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Switch(
                                checked = !isHiddenMode,
                                onCheckedChange = { active ->
                                    isHiddenMode = !active
                                    onHideToggle(active)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = HologramCyan,
                                    checkedTrackColor = ElectricBlue
                                )
                            )
                        }
                    }
                }

                FloatingTab.AUTO_FILL -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Status: $autoFillStatus",
                            color = HologramCyan,
                            fontSize = 11.sp
                        )

                        if (autoFillCountdown > 0) {
                            Text(
                                text = "⏳ Memasukkan teks dalam $autoFillCountdown detik...",
                                color = ArmorGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        if (autoFillPreviewText.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Pratinjau Jawaban:",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = autoFillPreviewText,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        isAutoFilling = false
                                        autoFillCountdown = 0
                                        autoFillStatus = "Dibatalkan oleh pengguna"
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Batal", fontSize = 11.sp, color = StatusBanned)
                                }

                                Button(
                                    onClick = { copyToClipboard(autoFillPreviewText) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                                ) {
                                    Text("Salin", fontSize = 11.sp, color = ObsidianNavy)
                                }
                            }
                        }

                        CyberButton(
                            text = if (isAutoFilling) "Menganalisis..." else "Dapatkan Jawaban & Isi",
                            onClick = {
                                scope.launch {
                                    isAutoFilling = true
                                    autoFillStatus = "Mengambil konteks kolom..."
                                    val hint = AIAccessibilityService.getFocusedFieldHint() ?: "Jawaban Soal"
                                    autoFillStatus = "Meminta jawaban optimal dari server..."

                                    val result = aiRepository.fillAnswer(hint, "Soal form aktif")
                                    result.onSuccess { res ->
                                        autoFillPreviewText = res.fill_text
                                        autoFillStatus = "Menunggu verifikasi 2 detik sebelum pengisian..."
                                        // Wait at least 2 seconds as required by specification
                                        for (i in 2 downTo 1) {
                                            autoFillCountdown = i
                                            delay(1000)
                                        }
                                        autoFillCountdown = 0

                                        val injected = AIAccessibilityService.insertTextIntoFocusedNode(res.fill_text)
                                        if (injected) {
                                            autoFillStatus = "✅ Berhasil dimasukkan ke kolom!"
                                        } else {
                                            autoFillStatus = "Kolom tidak mendukung injeksi langsung. Silakan tekan tombol Salin."
                                        }
                                        isAutoFilling = false
                                    }.onFailure { err ->
                                        autoFillStatus = "Error: ${err.message}"
                                        isAutoFilling = false
                                    }
                                }
                            },
                            isLoading = isAutoFilling,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FloatingTab.SOLVE_SCREEN -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Status: $solveStatusText",
                            color = HologramCyan,
                            fontSize = 11.sp
                        )

                        if (solvedAnswer.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Hasil Analisis:",
                                        color = ArmorGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Jawaban: $solvedAnswer",
                                        color = HologramCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Alasan singkat: $solvedReason",
                                        color = RobotSilver,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { copyToClipboard("Jawaban: $solvedAnswer\nAlasan: $solvedReason") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                                ) {
                                    Text("Salin", fontSize = 11.sp, color = ObsidianNavy)
                                }

                                OutlinedButton(
                                    onClick = {
                                        solvedAnswer = ""
                                        solveStatusText = "Siap mengambil tangkapan layar baru"
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Coba Lagi", fontSize = 11.sp, color = HologramCyan)
                                }
                            }
                        }

                        CyberButton(
                            text = if (isSolvingScreen) "Memproses..." else "Ambil Layar & Cari Jawaban",
                            onClick = {
                                scope.launch {
                                    isSolvingScreen = true
                                    solveStatusText = "Menangkap layar..."
                                    ScreenCaptureManager.captureScreen(context) { captureRes ->
                                        captureRes.onSuccess { base64 ->
                                            scope.launch {
                                                solveStatusText = "Mengirim gambar ke server..."
                                                delay(400)
                                                solveStatusText = "Membaca soal & opsi jawaban..."
                                                val apiRes = aiRepository.solveScreen(base64)
                                                apiRes.onSuccess { data ->
                                                    solveStatusText = "Menyusun jawaban..."
                                                    delay(300)
                                                    solvedAnswer = data.answer
                                                    solvedReason = data.reason
                                                    solvedQuestion = data.question ?: ""
                                                    solveStatusText = "Selesai ✅"
                                                    isSolvingScreen = false
                                                }.onFailure { err ->
                                                    solveStatusText = "Gagal: ${err.message}"
                                                    isSolvingScreen = false
                                                }
                                            }
                                        }.onFailure {
                                            solveStatusText = "Gagal menangkap layar"
                                            isSolvingScreen = false
                                        }
                                    }
                                }
                            },
                            isLoading = isSolvingScreen,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FloatingTab.ASK_AI -> {
                    Column(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (chatHistory.isEmpty()) {
                                item {
                                    Text(
                                        text = "Tanya apa saja kepada Malsz AI Robot...",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            items(chatHistory) { (msg, isUser) ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isUser) ElectricBlue.copy(alpha = 0.2f) else MidnightBlue,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isUser) ElectricBlue else CardBorder
                                        ),
                                        modifier = Modifier.widthIn(max = 240.dp)
                                    ) {
                                        Text(
                                            text = msg,
                                            color = if (isUser) TextPrimary else RobotSilver,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (isAskingAI) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = HologramCyan
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = askInput,
                                onValueChange = { askInput = it },
                                placeholder = { Text("Ketik pertanyaan...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, color = TextPrimary),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (askInput.isNotBlank() && !isAskingAI) {
                                        val q = askInput.trim()
                                        askInput = ""
                                        chatHistory.add(Pair(q, true))
                                        isAskingAI = true

                                        scope.launch {
                                            val historyList = chatHistory.map { ChatHistoryItem(it.first, it.second) }
                                            val res = aiRepository.ask(q, historyList)
                                            res.onSuccess { data ->
                                                chatHistory.add(Pair(data.answer, false))
                                                isAskingAI = false
                                            }.onFailure { err ->
                                                chatHistory.add(Pair("Error: ${err.message}", false))
                                                isAskingAI = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = ObsidianNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MidnightBlue,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = HologramCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
                Text(
                    text = desc,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
