package com.radwan.nova.ui.screens.chat

import com.radwan.nova.ui.components.EmojiPickerView

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.PaddingValues


import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Mood


import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Close
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.platform.LocalContext
import com.radwan.nova.utils.AudioHelper
import java.io.File
import kotlinx.coroutines.delay
import android.Manifest


import com.radwan.nova.data.local.LanguageManager

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.radwan.nova.data.remote.RemoteProfile
import com.radwan.nova.data.remote.SupabaseManager
import com.radwan.nova.viewmodel.ChatViewModel
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import java.time.Instant

// دوال إدارة الحظر محلياً وبشكل دائم
private fun isUserBlocked(context: Context, userId: String): Boolean {
    val prefs = context.getSharedPreferences("nova_chat_prefs", Context.MODE_PRIVATE)
    val blockedSet = prefs.getStringSet("blocked_users", emptySet()) ?: emptySet()
    return blockedSet.contains(userId)
}

private fun setUserBlocked(context: Context, userId: String, blocked: Boolean) {
    val prefs = context.getSharedPreferences("nova_chat_prefs", Context.MODE_PRIVATE)
    val blockedSet = prefs.getStringSet("blocked_users", emptySet())?.toMutableSet() ?: mutableSetOf()
    if (blocked) {
        blockedSet.add(userId)
    } else {
        blockedSet.remove(userId)
    }
    prefs.edit().putStringSet("blocked_users", blockedSet).apply()
}

// دوال حفظ توقيت مسح المحادثة محلياً
private fun getClearedChatTime(context: Context, chatId: String): Long {
    val prefs = context.getSharedPreferences("nova_chat_prefs", Context.MODE_PRIVATE)
    return prefs.getLong("cleared_$chatId", 0L)
}

private fun setClearedChatTime(context: Context, chatId: String, timestamp: Long) {
    val prefs = context.getSharedPreferences("nova_chat_prefs", Context.MODE_PRIVATE)
    prefs.edit().putLong("cleared_$chatId", timestamp).apply()
}

private fun parseMessageTime(ts: String?): Long {
    if (ts.isNullOrBlank()) return 0L
    return try {
        Instant.parse(ts).toEpochMilli()
    } catch (e: Exception) {
        try {
            ts.toLong()
        } catch (e2: Exception) {
            0L
        }
    }
}


fun parseImageModel(data: String?): Any? {
    if (data.isNullOrBlank()) return null
    return if (data.startsWith("data:image") && data.contains(",")) {
        try {
            val base64Str = data.substringAfter(",")
            android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            data
        }
    } else {
        data
    }
}

fun formatDisplayTime(raw: String?): String {
    val fallback = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date())
    if (raw.isNullOrBlank()) return fallback
    val str = raw.trim().replace("\"", "")
    try {
        val millis = str.toLongOrNull()
        if (millis != null && millis > 1000000000L) {
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getDefault()
            return sdf.format(java.util.Date(millis))
        }
    } catch (e: Exception) {}
    return fallback
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    chatTitle: String,
    onBackClick: () -> Unit = {},
    
    viewModel: ChatViewModel = viewModel()
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allMessages by viewModel.messages.collectAsState()
    val isOtherOnline by viewModel.otherUserOnline.collectAsState()
    var isEmojiPickerVisible by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    var isRecording by remember { mutableStateOf(false) }
    var recordDuration by remember { mutableIntStateOf(0) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            currentAudioFile = audioFile
            if (AudioHelper.startRecording(context, audioFile)) {
                isRecording = true
                recordDuration = 0
            } else {
                Toast.makeText(context, "تم مسح المحادثة", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "تم مسح المحادثة", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordDuration++
            }
        }
    }

    val listState = rememberLazyListState()
    val currentUserId = SupabaseManager.auth.currentUserOrNull()?.id ?: ""

    // حالات الحظر ومسح المحادثة
    var isBlocked by remember { mutableStateOf(isUserBlocked(context, chatId)) }
    var lastClearedTime by remember { mutableLongStateOf(getClearedChatTime(context, chatId)) }

    // القائمة المنسدلة والحوارات
    var menuExpanded by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showUnblockConfirmDialog by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var otherUserProfile by remember { mutableStateOf<RemoteProfile?>(null) }

    // تصفية الرسائل بحيث لا تظهر الرسائل الممسوحة سابقاً
    val displayedMessages = remember(allMessages, lastClearedTime) {
        val visible = if (lastClearedTime == 0L) {
            allMessages
        } else {
            allMessages.filter { msg ->
                val time = parseMessageTime(msg.timestamp)
                time == 0L || time > lastClearedTime
            }
        }
        visible.filter { it.text?.startsWith("[CALL_") != true }
    }

    LaunchedEffect(chatId) {
        if (chatId.isNotBlank()) {
            viewModel.loadMessages(chatId)
            isBlocked = isUserBlocked(context, chatId)
            lastClearedTime = getClearedChatTime(context, chatId)
            try {
                val profile = SupabaseManager.postgrest.from("profiles")
                    .select {
                        filter {
                            eq("id", chatId)
                        }
                    }.decodeSingleOrNull<RemoteProfile>()
                otherUserProfile = profile
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(displayedMessages.size) {
        if (displayedMessages.isNotEmpty()) {
            listState.animateScrollToItem(displayedMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showProfileDialog = true }
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = Color(0xFF2563EB)
                        ) {
                            val avatarModel = parseImageModel(otherUserProfile?.avatar_url)
                            if (avatarModel != null) {
                                AsyncImage(
                                    model = avatarModel,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chatTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isBlocked) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "محظور",
                                            color = Color(0xFFEF4444),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isBlocked) "تم حظر هذا المستخدم" else if (isOtherOnline) LanguageManager.getString("online") else LanguageManager.getString("offline"),
                                fontSize = 11.sp,
                                color = if (isBlocked) Color(0xFFEF4444) else if (isOtherOnline) Color(0xFF10B981) else Color.LightGray
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    
            

                    // أيقونة المزيد من الخيارات (⋮)
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "المزيد من الخيارات",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            // 1. عرض الملف الشخصي
                    DropdownMenuItem(
                        text = { Text(LanguageManager.getString("contact_info"), color = Color.White, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color(0xFF60A5FA))
                        },
                        onClick = {
                            menuExpanded = false
                            showProfileDialog = true
                        }
                    )

                            // 2. زر الحظر / فك الحظر
                            if (isBlocked) {
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.getString("unblock"), color = Color(0xFF10B981), fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF10B981))
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        showUnblockConfirmDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text(LanguageManager.getString("block"), color = Color(0xFFEF4444), fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEF4444))
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        showBlockConfirmDialog = true
                                    }
                                )
                            }

                            // 3. مسح محتوى المحادثة
                            DropdownMenuItem(
                                text = { Text(LanguageManager.getString("clear_chat"), color = Color(0xFFF87171), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFF87171))
                                },
                                onClick = {
                                    menuExpanded = false
                                    showClearChatDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E293B))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (displayedMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد رسائل حالياً في هذه المحادثة",
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(displayedMessages) { msg ->
                        val isMe = msg.senderId == currentUserId
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) AbsoluteAlignment.CenterRight else AbsoluteAlignment.CenterLeft
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                color = if (isMe) Color(0xFF2563EB) else Color(0xFF334155),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val isVoice = msg.text?.startsWith("[VOICE]:") == true
                            if (isVoice) {
                                val voicePayload = msg.text?.removePrefix("[VOICE]:") ?: ""
                                val voiceUrl = voicePayload.substringBefore("|")
                                val voiceSec = voicePayload.substringAfter("|", "0").toIntOrNull() ?: 0
                                val isPlayingThis = currentlyPlayingUrl == voiceUrl

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPlayingThis) {
                                                AudioHelper.stopPlaying()
                                                currentlyPlayingUrl = null
                                            } else {
                                                currentlyPlayingUrl = voiceUrl
                                                AudioHelper.playAudio(context, voiceUrl) {
                                                    currentlyPlayingUrl = null
                                                }
                                            }
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = if (isMe) Color.White else Color(0xFF2563EB)
                                        ),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Voice",
                                            tint = if (isMe) Color(0xFF2563EB) else Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(14) { i ->
                                                val h = (6 + (i * 3) % 14).dp
                                                Box(
                                                    modifier = Modifier
                                                        .padding(horizontal = 1.dp)
                                                        .width(3.dp)
                                                        .height(h)
                                                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = String.format("%02d:%02d", voiceSec / 60, voiceSec % 60),
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = msg.text ?: "",
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                                    Row(
                                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (!msg.timestamp.isNullOrBlank()) {
                                            Text(
                                                text = formatDisplayTime(msg.timestamp),
                                                color = Color.LightGray.copy(alpha = 0.8f),
                                                fontSize = 10.sp
                                            )
                                        }
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (msg.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                                                contentDescription = if (msg.isRead) "قُرئت" else "أُرسلت",
                                                tint = if (msg.isRead) Color(0xFF38BDF8) else Color.LightGray.copy(alpha = 0.7f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // الشريط السفلي
            Surface(
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBlocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageManager.getString("blocked_user_notice"),
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Button(
                            onClick = {
                                setUserBlocked(context, chatId, false)
                                isBlocked = false
                                Toast.makeText(context, "تم مسح المحادثة", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء الحظر", color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    if (isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                AudioHelper.stopRecording()
                                currentAudioFile?.delete()
                                currentAudioFile = null
                                isRecording = false
                                recordDuration = 0
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Red)
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%02d:%02d", recordDuration / 60, recordDuration % 60),
                            color = Color.White,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                AudioHelper.stopRecording()
                                isRecording = false
                                val f = currentAudioFile
                                if (f != null && f.exists() && f.length() > 0) {
                                    val b64 = AudioHelper.fileToBase64(f)
                                    val payload = "[VOICE]:$b64|$recordDuration"
                                    viewModel.sendMessage(chatId, payload)
                                    f.delete()
                                }
                                currentAudioFile = null
                                recordDuration = 0
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Voice", tint = Color.White)
                        }
                    }
                                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    isEmojiPickerVisible = !isEmojiPickerVisible
                                }
                            ) {
                                Text(
                                    text = if (isEmojiPickerVisible) "⌨️" else "😊",
                                    fontSize = 22.sp
                                )
                            }
                            TextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                placeholder = {
                                    Text(
                                        LanguageManager.getString("type_message"),
                                        color = Color(0xFF64748B)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (messageText.isBlank()) {
                                IconButton(
                                    onClick = {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = "Record Voice", tint = Color.White)
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        if (messageText.isNotBlank()) {
                                            viewModel.sendMessage(chatId, messageText)
                                            messageText = ""
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                                }
                            }
                        }
                    }
                }
                if (isEmojiPickerVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(270.dp)
                    ) {
                        EmojiPickerView(
                            onEmojiSelected = { emoji: String -> messageText += emoji },
                            onBackspace = {
                                if (messageText.isNotEmpty()) {
                                    messageText = messageText.dropLast(1)
                                }
                            }
                        )
                    }
                }
            }
        }

        // ========================================================
    if (showProfileDialog) {
        val displayName = otherUserProfile?.full_name?.takeIf { it.isNotBlank() }
            ?: otherUserProfile?.name?.takeIf { it.isNotBlank() }
            ?: chatTitle
        val displayUsername = otherUserProfile?.username?.takeIf { it.isNotBlank() } ?: "user"

        Dialog(
            onDismissRequest = { showProfileDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0B1120)
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = LanguageManager.getString("contact_info"),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { showProfileDialog = false }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                        )
                    },
                    containerColor = Color(0xFF0B1120)
                ) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            // صورة البروفايل الكبيرة ومؤشر الاتصال
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Surface(
                                    modifier = Modifier.size(130.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(3.dp, Color(0xFF2563EB))
                                ) {
                                    val avatarModel = parseImageModel(otherUserProfile?.avatar_url)
                                    if (avatarModel != null) {
                                        AsyncImage(
                                            model = avatarModel,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.padding(26.dp)
                                        )
                                    }
                                }

                                if (isOtherOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF22C55E), CircleShape)
                                            .border(3.dp, Color(0xFF0B1120), CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "@$displayUsername",
                                fontSize = 14.sp,
                                color = Color(0xFF60A5FA)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isBlocked) "محظور حالياً" else if (isOtherOnline) LanguageManager.getString("online") + " 🟢" else LanguageManager.getString("offline") + " ⚪",
                                fontSize = 12.sp,
                                color = if (isBlocked) Color(0xFFEF4444) else if (isOtherOnline) Color(0xFF22C55E) else Color(0xFF94A3B8)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // شريط الإجراءات السريعة (WhatsApp Action Bar)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // مراسلة
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { showProfileDialog = false }
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Chat,
                                            contentDescription = LanguageManager.getString("message"),
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(LanguageManager.getString("message"), color = Color(0xFF94A3B8), fontSize = 12.sp)
                                }
                                

                                // حظر
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        showProfileDialog = false
                                        if (isBlocked) showUnblockConfirmDialog = true else showBlockConfirmDialog = true
                                    }
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Icon(
                                            if (isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                                            contentDescription = "حظر",
                                            tint = if (isBlocked) Color(0xFF22C55E) else Color(0xFFEF4444),
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(if (isBlocked) LanguageManager.getString("unblock") else LanguageManager.getString("block"), color = if (isBlocked) Color(0xFF22C55E) else Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }
                        }

                        // بطاقة الحالة / النبذة (About)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = LanguageManager.getString("about_section"),
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFF60A5FA),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = otherUserProfile?.bio?.ifBlank { "Hey there! I am using NOVA Chat." }
                                                ?: "Hey there! I am using NOVA Chat.",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // بطاقة التشفير التام (E2EE)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.EnhancedEncryption,
                                        contentDescription = null,
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = LanguageManager.getString("encryption_title"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = LanguageManager.getString("encryption_desc"),
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        // بطاقة مسح المحادثة
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showProfileDialog = false
                                        showClearChatDialog = true
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = LanguageManager.getString("clear_chat"),
                                        color = Color(0xFFEF4444),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    // 🚫 2. نافذة تأكيد الحظر
    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            title = { Text(LanguageManager.getString("block"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حظر $chatTitle؟ لن تتمكن من إرسال أو استلام رسائل منه.", color = Color(0xFFCBD5E1)) },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        setUserBlocked(context, chatId, true)
                        isBlocked = true
                        showBlockConfirmDialog = false
                        Toast.makeText(context, "تم مسح المحادثة", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(LanguageManager.getString("block"), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text(LanguageManager.getString("cancel"), color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 🔓 3. نافذة تأكيد فك الحظر
    if (showUnblockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUnblockConfirmDialog = false },
            title = { Text(LanguageManager.getString("unblock"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("هل تريد إلغاء حظر $chatTitle والتمكن من التواصل معه مجدداً؟", color = Color(0xFFCBD5E1)) },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        setUserBlocked(context, chatId, false)
                        isBlocked = false
                        showUnblockConfirmDialog = false
                        Toast.makeText(context, "تم مسح المحادثة", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("إلغاء الحظر", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnblockConfirmDialog = false }) {
                    Text("رجوع", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    // 🗑️ 4. نافذة تأكيد مسح المحادثة الفعلي
    
        
    
        if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = { Text(LanguageManager.getString("clear_chat"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من رغبتك في حذف جميع الرسائل في هذه المحادثة؟ لا يمكن التراجع عن هذا الإجراء.", color = Color(0xFFCBD5E1)) },
            containerColor = Color(0xFF1E293B),
            confirmButton = {
                Button(
                    onClick = {
                        val now = System.currentTimeMillis()
                        setClearedChatTime(context, chatId, now)
                        lastClearedTime = now
                        showClearChatDialog = false

                        scope.launch {
                            try {
                                SupabaseManager.postgrest.from("messages").delete {
                                    filter {
                                        eq("sender_id", currentUserId)
                                        eq("receiver_id", chatId)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            try {
                                SupabaseManager.postgrest.from("messages").delete {
                                    filter {
                                        eq("sender_id", chatId)
                                        eq("receiver_id", currentUserId)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            Toast.makeText(context, "تم مسح المحادثة", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(LanguageManager.getString("clear_now"), color = Color.White)
                }
            TextButton(onClick = { showClearChatDialog = false }) {
                Text(LanguageManager.getString("cancel"), color = Color(0xFF94A3B8))
            }
        }
    )

    }



}
}
