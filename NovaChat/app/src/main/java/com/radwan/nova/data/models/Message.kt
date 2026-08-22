package com.radwan.nova.data.models

enum class MessageType {
    TEXT, IMAGE, VIDEO, DOCUMENT, VOICE, LOCATION, CONTACT
}

data class Reaction(
    val emoji: String,
    val userId: String,
    val userName: String
)

data class ReminderInfo(
    val timeMillis: Long,
    val label: String
)

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String? = null,
    val messageType: MessageType = MessageType.TEXT,
    val timestamp: String,
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isPinned: Boolean = false,
    val isSaved: Boolean = false,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val attachmentUrl: String? = null,
    val durationSeconds: Int? = null,
    val reactions: List<Reaction> = emptyList(),
    val reminder: ReminderInfo? = null,
    val expiresAtMillis: Long? = null
)
