package com.radwan.nova.data.models

data class Chat(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val lastSeen: String? = null,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isGroup: Boolean = false,
    val membersCount: Int? = null,
    val typingStatus: String? = null,
    val bio: String? = null,
    val phone: String? = null,
    val username: String? = null
)
