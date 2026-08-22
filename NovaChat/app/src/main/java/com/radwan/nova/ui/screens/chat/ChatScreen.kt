package com.radwan.nova.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.radwan.nova.ui.components.*
import com.radwan.nova.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    chatName: String = "Marquesh",
    chatAvatar: String = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&q=80",
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val selectedMessage by viewModel.selectedMessageForAction.collectAsState()
    var showAttachmentSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = chatAvatar,
                            contentDescription = chatName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = chatName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Online",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onVoiceCallClick) {
                        Icon(Icons.Outlined.Call, contentDescription = "Voice Call")
                    }
                    IconButton(onClick = onVideoCallClick) {
                        Icon(Icons.Outlined.Videocam, contentDescription = "Video Call")
                    }
                    IconButton(onClick = { /* More Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Messages Bubble List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            message = msg,
                            isSender = msg.senderId == "user_radwan",
                            onLongClick = { viewModel.openMessageActions(msg) }
                        )
                    }
                }

                // Chat Input Bar (Text + Voice + Attachments)
                ChatInputBar(
                    text = inputText,
                    onTextChanged = { viewModel.onInputTextChanged(it) },
                    onSendClick = { viewModel.sendMessage() },
                    onAttachClick = { showAttachmentSheet = true },
                    onVoiceRecordComplete = { duration -> viewModel.sendVoiceMessage(duration) }
                )
            }

            // Message Actions Bottom Sheet (Reply, React, Remind me, Save, Pin)
            if (selectedMessage != null) {
                MessageActionsBottomSheet(
                    message = selectedMessage!!,
                    onDismiss = { viewModel.closeMessageActions() },
                    onReactionSelect = { emoji -> viewModel.addReaction(emoji) },
                    onRemindMeSelect = { minutes -> viewModel.setReminder(minutes) },
                    onPinClick = { viewModel.togglePin() },
                    onSaveClick = { viewModel.toggleSave() },
                    onDeleteClick = { viewModel.deleteMessage() }
                )
            }

            // Attachment Menu Bottom Sheet
            if (showAttachmentSheet) {
                AttachmentBottomSheet(
                    onDismiss = { showAttachmentSheet = false },
                    onOptionSelected = { option ->
                        showAttachmentSheet = false
                        if (option == "Gallery") {
                            viewModel.sendImage(
                                "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&q=80",
                                "Scenic photo from gallery 📷"
                            )
                        }
                    }
                )
            }
        }
    }
}
