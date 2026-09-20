package com.radwan.nova.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radwan.nova.data.models.Message
import com.radwan.nova.data.remote.SupabaseManager
import com.radwan.nova.data.remote.RemoteMessage
import com.radwan.nova.data.remote.RemoteProfile
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatViewModel : ViewModel() {

    private fun parseToLocalTime(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val str = raw.trim()
        return try {
            val parsers = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
            )
            var parsedDate: java.util.Date? = null
            for (p in parsers) {
                try {
                    val sdf = java.text.SimpleDateFormat(p, java.util.Locale.US)
                    if (str.contains("+") || str.contains("Z")) {
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                    val d = sdf.parse(str)
                    if (d != null) {
                        parsedDate = d
                        break
                    }
                } catch (e: Exception) {}
            }
            val target = parsedDate ?: java.util.Date()
            val outSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            outSdf.timeZone = java.util.TimeZone.getDefault()
            outSdf.format(target)
        } catch (e: Exception) {
            val outSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            outSdf.format(java.util.Date())
        }
    }


    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _otherUserOnline = MutableStateFlow(false)
    val otherUserOnline: StateFlow<Boolean> = _otherUserOnline.asStateFlow()

    private var activeRoomId: String? = null
    private var targetOtherId: String? = null

    // توليد معرف موحد وصارم بين أي مستخدمين بالترتيب الأبجدي لمعرفاتهم
    fun generateRoomId(userA: String, userB: String): String {
        val a = userA.trim().lowercase()
        val b = userB.trim().lowercase()
        return if (a < b) "${a}__${b}" else "${b}__${a}"
    }

    fun loadMessages(otherUserIdOrRoom: String) {
        if (otherUserIdOrRoom.isBlank()) return

        val myId = SupabaseManager.auth.currentUserOrNull()?.id ?: ""
        
        // تحديد معرف الغرفة الحقيقي ومعرف الطرف الآخر
        val roomId = if (otherUserIdOrRoom.contains("__")) {
            val parts = otherUserIdOrRoom.split("__")
            targetOtherId = parts.firstOrNull { it != myId } ?: parts[0]
            otherUserIdOrRoom
        } else {
            targetOtherId = otherUserIdOrRoom
            if (myId.isNotBlank()) generateRoomId(myId, otherUserIdOrRoom) else otherUserIdOrRoom
        }

        activeRoomId = roomId

        viewModelScope.launch {
            _isLoading.value = true
            fetchMessages(roomId)
            checkOtherUserStatus()
            _isLoading.value = false

            // مزامنة حية كل 1.5 ثانية للرسائل وحالة الاتصال
            while (isActive && activeRoomId == roomId) {
                delay(1500)
                fetchMessages(roomId)
                checkOtherUserStatus()
            }
        }
    }

    private suspend fun checkOtherUserStatus() {
        val otherId = targetOtherId ?: return
        try {
            val profile = SupabaseManager.postgrest["profiles"]
                .select {
                    filter {
                        or {
                            eq("id", otherId)
                            eq("username", otherId)
                            eq("email", otherId)
                        }
                    }
                }
                .decodeSingleOrNull<RemoteProfile>()
            
            _otherUserOnline.value = profile?.is_online == true
        } catch (e: Exception) {
            _otherUserOnline.value = false
        }
    }

    private suspend fun fetchMessages(roomId: String) {
        try {
            val remoteList = SupabaseManager.postgrest["messages"]
                .select {
                    filter {
                        eq("chat_id", roomId)
                    }
                }
                .decodeList<RemoteMessage>()

            val mapped = remoteList.map { r ->
                Message(
                    id = r.id ?: UUID.randomUUID().toString(),
                    chatId = r.chat_id,
                    senderId = r.sender_id ?: "",
                    senderName = r.sender_name ?: "مستخدم",
                    text = r.text,
                    timestamp = parseToLocalTime(r.created_at),
                    isRead = r.is_read
                )
            }

            _messages.value = mapped

            // تحديث حالة القراءة للرسائل الواردة من الطرف الآخر
            val myId = SupabaseManager.auth.currentUserOrNull()?.id
            val hasUnreadFromOther = remoteList.any { it.sender_id != myId && !it.is_read }
            if (hasUnreadFromOther && myId != null) {
                viewModelScope.launch {
                    try {
                        SupabaseManager.postgrest["messages"].update(
                            mapOf("is_read" to true)
                        ) {
                            filter {
                                eq("chat_id", roomId)
                                neq("sender_id", myId)
                                eq("is_read", false)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(otherUserIdOrRoom: String, text: String) {
        if (text.isBlank() || otherUserIdOrRoom.isBlank()) return

        val myUser = SupabaseManager.auth.currentUserOrNull()
        val myId = myUser?.id ?: "guest"
        val myName = myUser?.email?.substringBefore("@") ?: "مستخدم"

        val roomId = if (otherUserIdOrRoom.contains("__")) {
            otherUserIdOrRoom
        } else {
            generateRoomId(myId, otherUserIdOrRoom)
        }

        val newMsgId = UUID.randomUUID().toString()
        val currentTimeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        val newMsg = Message(
            id = newMsgId,
            chatId = roomId,
            senderId = myId,
            senderName = myName,
            text = text.trim(),
            timestamp = currentTimeStr
        )
        
        _messages.value = _messages.value + newMsg

        viewModelScope.launch {
            try {
                val remote = RemoteMessage(
                    id = newMsgId,
                    chat_id = roomId,
                    sender_id = myId,
                    text = text.trim(),
                    sender_name = myName
                )
                SupabaseManager.postgrest["messages"].insert(remote)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
