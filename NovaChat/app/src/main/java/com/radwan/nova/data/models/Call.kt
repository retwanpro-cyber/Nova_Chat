package com.radwan.nova.data.models

enum class CallType {
    INCOMING, OUTGOING, MISSED
}

data class Call(
    val id: String,
    val contactId: String,
    val contactName: String,
    val avatarUrl: String,
    val type: CallType,
    val isVideo: Boolean,
    val timestamp: String,
    val duration: String? = null
)
