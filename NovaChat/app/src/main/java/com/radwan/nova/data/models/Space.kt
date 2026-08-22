package com.radwan.nova.data.models

enum class ChannelType {
    TEXT, ANNOUNCEMENTS, VOICE, FILES
}

data class SpaceChannel(
    val id: String,
    val name: String,
    val type: ChannelType,
    val unreadCount: Int = 0
)

data class Space(
    val id: String,
    val name: String,
    val logoEmoji: String,
    val description: String,
    val membersCount: Int,
    val category: String,
    val channels: List<SpaceChannel>,
    val isJoined: Boolean = false
)
