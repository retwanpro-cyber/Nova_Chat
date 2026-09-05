package com.radwan.nova.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radwan.nova.data.remote.RemoteMessage
import com.radwan.nova.data.remote.SupabaseManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UiMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true
)

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUserId = SupabaseManager.auth.currentUserOrNull()?.id ?: ""
                val remoteList = SupabaseManager.postgrest["messages"]
                    .select {
                        filter {
                            eq("chat_id", chatId)
                        }
                    }
                    .decodeList<RemoteMessage>()

                _messages.value = remoteList.map { r ->
                    UiMessage(
                        id = r.id ?: UUID.randomUUID().toString(),
                        chatId = r.chat_id,
                        senderId = r.sender_id,
                        text = r.text,
                        timestamp = System.currentTimeMillis(),
                        isFromMe = (r.sender_id == currentUserId || currentUserId.isEmpty())
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        if (content.isBlank()) return

        val user = SupabaseManager.auth.currentUserOrNull()
        val currentUserId = user?.id ?: "00000000-0000-0000-0000-000000000000"
        val newMsgId = UUID.randomUUID().toString()

        // إضافة فورية للشاشة تظهر أمامك
        val newMsg = UiMessage(
            id = newMsgId,
            chatId = chatId,
            senderId = currentUserId,
            text = content.trim(),
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        _messages.value = _messages.value + newMsg

        // حفظ في جدول messages في Supabase
        viewModelScope.launch {
            try {
                val remote = RemoteMessage(
                    id = newMsgId,
                    chat_id = chatId,
                    sender_id = currentUserId,
                    text = content.trim(),
                    sender_name = user?.email?.substringBefore("@") ?: "مستخدم"
                )
                SupabaseManager.postgrest["messages"].insert(remote)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
