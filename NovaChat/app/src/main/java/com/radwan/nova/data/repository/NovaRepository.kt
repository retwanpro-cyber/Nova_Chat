package com.radwan.nova.data.repository

import com.radwan.nova.data.models.*
import kotlinx.coroutines.flow.Flow

interface NovaRepository {
    fun getChats(): Flow<List<Chat>>
    fun getMessages(chatId: String): Flow<List<Message>>
    fun getCalls(): Flow<List<Call>>
    fun getSpaces(): Flow<List<Space>>
    suspend fun sendMessage(chatId: String, text: String, type: MessageType, attachmentUrl: String? = null)
    suspend fun deleteMessage(messageId: String)
    suspend fun togglePinMessage(messageId: String)
    suspend fun toggleSaveMessage(messageId: String)
    suspend fun addReaction(messageId: String, emoji: String)
    suspend fun setReminder(messageId: String, durationMinutes: Int)
}
