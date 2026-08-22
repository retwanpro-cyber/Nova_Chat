package com.radwan.nova.data.repository

import com.radwan.nova.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class FakeNovaRepository : NovaRepository {

    private val currentUserId = "user_radwan"

    private val _chats = MutableStateFlow<List<Chat>>(
        listOf(
            Chat(
                id = "chat_marquesh",
                name = "Marquesh",
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&q=80",
                lastMessage = "🎙️ Voice message (0:14)",
                lastMessageTime = "3:02 PM",
                unreadCount = 0,
                isOnline = true,
                isPinned = true,
                isGroup = false,
                bio = "Photography & AI Architect 📸",
                phone = "+1 555 019 2831",
                username = "marquesh_dev"
            ),
            Chat(
                id = "chat_tech",
                name = "Tech Community ⚡",
                avatarUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=200&q=80",
                lastMessage = "Omar: Check the new Jetpack Compose architecture update!",
                lastMessageTime = "2:45 PM",
                unreadCount = 12,
                isOnline = true,
                isPinned = true,
                isGroup = true,
                membersCount = 1420
            ),
            Chat(
                id = "chat_natalia",
                name = "Natalia Chari",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80",
                lastMessage = "What are you doing tonight?",
                lastMessageTime = "1:30 PM",
                unreadCount = 2,
                isOnline = true,
                isPinned = false,
                isGroup = false,
                phone = "+44 7911 123456",
                username = "natalia_c"
            ),
            Chat(
                id = "chat_leaner",
                name = "Leaner Brooks",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&q=80",
                lastMessage = "Hi, Broo! Did you push the latest commit?",
                lastMessageTime = "1:00 PM",
                unreadCount = 0,
                isOnline = false,
                lastSeen = "1:05 PM",
                isPinned = false,
                isGroup = false
            )
        )
    )

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(
        mapOf(
            "chat_marquesh" to listOf(
                Message(
                    id = "m1",
                    chatId = "chat_marquesh",
                    senderId = "marquesh",
                    senderName = "Marquesh",
                    text = "Hi! Where are you going tonight?",
                    messageType = MessageType.TEXT,
                    timestamp = "1:00 PM",
                    isRead = true,
                    reactions = listOf(Reaction("👍", "user_radwan", "Radwan"))
                ),
                Message(
                    id = "m2",
                    chatId = "chat_marquesh",
                    senderId = currentUserId,
                    senderName = "Radwan Almsora",
                    text = "I will send you the location in a bit!",
                    messageType = MessageType.TEXT,
                    timestamp = "1:05 PM",
                    isRead = true
                ),
                Message(
                    id = "m3",
                    chatId = "chat_marquesh",
                    senderId = "marquesh",
                    senderName = "Marquesh",
                    text = "Send a new place to visit 🌍",
                    messageType = MessageType.TEXT,
                    timestamp = "2:20 PM",
                    isRead = true,
                    reactions = listOf(Reaction("❤️", "user_radwan", "Radwan"))
                ),
                Message(
                    id = "m4",
                    chatId = "chat_marquesh",
                    senderId = currentUserId,
                    senderName = "Radwan Almsora",
                    text = "Check this breathtaking coastal view! Perfect spot for relaxing.",
                    messageType = MessageType.IMAGE,
                    attachmentUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80",
                    timestamp = "2:50 PM",
                    isRead = true,
                    reactions = listOf(Reaction("🔥", "marquesh", "Marquesh"))
                ),
                Message(
                    id = "m5",
                    chatId = "chat_marquesh",
                    senderId = "marquesh",
                    senderName = "Marquesh",
                    messageType = MessageType.VOICE,
                    durationSeconds = 14,
                    timestamp = "3:02 PM",
                    isRead = true
                )
            )
        )
    )

    private val _calls = MutableStateFlow<List<Call>>(
        listOf(
            Call(
                id = "c1",
                contactId = "marquesh",
                contactName = "Marquesh",
                avatarUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&q=80",
                type = CallType.INCOMING,
                isVideo = false,
                timestamp = "Today, 2:10 PM",
                duration = "4:15 min"
            ),
            Call(
                id = "c2",
                contactId = "natalia",
                contactName = "Natalia Chari",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&q=80",
                type = CallType.MISSED,
                isVideo = true,
                timestamp = "Yesterday, 8:45 PM"
            )
        )
    )

    private val _spaces = MutableStateFlow<List<Space>>(
        listOf(
            Space(
                id = "sp_tech",
                name = "Android Developers & Kotlin",
                logoEmoji = "🤖",
                description = "The premier community for Android architects & Jetpack Compose masters.",
                membersCount = 8420,
                category = "Technology",
                channels = listOf(
                    SpaceChannel("ch1", "general-chat", ChannelType.TEXT, 3),
                    SpaceChannel("ch2", "announcements", ChannelType.ANNOUNCEMENTS),
                    SpaceChannel("ch3", "Dev Voice Lounge", ChannelType.VOICE)
                ),
                isJoined = true
            ),
            Space(
                id = "sp_gaming",
                name = "NOVA Gaming Hub",
                logoEmoji = "🎮",
                description = "Tournaments, clips, and competitive squads.",
                membersCount = 5120,
                category = "Gaming",
                channels = listOf(
                    SpaceChannel("ch_g1", "squad-chat", ChannelType.TEXT)
                ),
                isJoined = true
            )
        )
    )

    override fun getChats(): Flow<List<Chat>> = _chats.asStateFlow()

    override fun getMessages(chatId: String): Flow<List<Message>> {
        val currentMap = _messages.value
        val list = currentMap[chatId] ?: emptyList()
        return MutableStateFlow(list).asStateFlow()
    }

    override fun getCalls(): Flow<List<Call>> = _calls.asStateFlow()

    override fun getSpaces(): Flow<List<Space>> = _spaces.asStateFlow()

    override suspend fun sendMessage(chatId: String, text: String, type: MessageType, attachmentUrl: String?) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeNow = sdf.format(Date())

        val newMessage = Message(
            id = "msg_" + UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = currentUserId,
            senderName = "Radwan Almsora",
            text = text,
            messageType = type,
            timestamp = timeNow,
            isRead = false,
            attachmentUrl = attachmentUrl
        )

        val currentMap = _messages.value.toMutableMap()
        val currentList = (currentMap[chatId] ?: emptyList()).toMutableList()
        currentList.add(newMessage)
        currentMap[chatId] = currentList
        _messages.value = currentMap
    }

    override suspend fun deleteMessage(messageId: String) {
        val currentMap = _messages.value.toMutableMap()
        for ((chatId, list) in currentMap) {
            val filtered = list.filter { it.id != messageId }
            currentMap[chatId] = filtered
        }
        _messages.value = currentMap
    }

    override suspend fun togglePinMessage(messageId: String) {
        val currentMap = _messages.value.toMutableMap()
        for ((chatId, list) in currentMap) {
            val updated = list.map {
                if (it.id == messageId) it.copy(isPinned = !it.isPinned) else it
            }
            currentMap[chatId] = updated
        }
        _messages.value = currentMap
    }

    override suspend fun toggleSaveMessage(messageId: String) {
        val currentMap = _messages.value.toMutableMap()
        for ((chatId, list) in currentMap) {
            val updated = list.map {
                if (it.id == messageId) it.copy(isSaved = !it.isSaved) else it
            }
            currentMap[chatId] = updated
        }
        _messages.value = currentMap
    }

    override suspend fun addReaction(messageId: String, emoji: String) {
        val currentMap = _messages.value.toMutableMap()
        for ((chatId, list) in currentMap) {
            val updated = list.map { msg ->
                if (msg.id == messageId) {
                    val existing = msg.reactions.filter { it.userId != currentUserId }
                    val newReactions = existing + Reaction(emoji, currentUserId, "Radwan")
                    msg.copy(reactions = newReactions)
                } else msg
            }
            currentMap[chatId] = updated
        }
        _messages.value = currentMap
    }

    override suspend fun setReminder(messageId: String, durationMinutes: Int) {
        val currentMap = _messages.value.toMutableMap()
        val targetTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val label = "Reminder in $durationMinutes min"

        for ((chatId, list) in currentMap) {
            val updated = list.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(reminder = ReminderInfo(targetTime, label))
                } else msg
            }
            currentMap[chatId] = updated
        }
        _messages.value = currentMap
    }
}
