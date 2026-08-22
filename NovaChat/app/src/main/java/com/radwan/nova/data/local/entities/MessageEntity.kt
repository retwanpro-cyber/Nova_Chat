package com.radwan.nova.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.radwan.nova.data.models.MessageType

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String?,
    val messageType: MessageType,
    val timestamp: Long,
    val isRead: Boolean,
    val isEdited: Boolean,
    val isPinned: Boolean,
    val isSaved: Boolean,
    val replyToId: String?,
    val attachmentUrl: String?,
    val durationSeconds: Int?,
    val expiresAt: Long?
)
