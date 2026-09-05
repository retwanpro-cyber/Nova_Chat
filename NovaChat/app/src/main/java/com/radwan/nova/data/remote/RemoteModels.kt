package com.radwan.nova.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteProfile(
    val id: String = "",
    val username: String = "",
    val full_name: String = "",
    val name: String = "",
    val email: String? = null,
    val avatar_url: String? = null,
    val bio: String? = null,
    val status: String? = null,
    val is_online: Boolean = false,
    val created_at: String? = null
)

@Serializable
data class RemoteMessage(
    val id: String? = null,
    val chat_id: String = "",
    val sender_id: String = "",
    val text: String = "",
    val created_at: String? = null,
    val sender_name: String? = null
)
