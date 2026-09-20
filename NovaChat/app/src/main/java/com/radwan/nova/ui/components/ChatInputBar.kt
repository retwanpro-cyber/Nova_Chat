package com.radwan.nova.ui.components

import com.radwan.nova.data.local.LanguageManager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.radwan.nova.R

@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    onVoiceRecordComplete: (Int) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Emoji Picker */ }) {
                Icon(
                    Icons.Outlined.SentimentSatisfied,
                    contentDescription = "Emojis",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        stringResource(R.string.type_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4
            )

            IconButton(onClick = onAttachClick) {
                Icon(
                    Icons.Outlined.AttachFile,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (text.isNotBlank()) {
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            } else {
                IconButton(
                    onClick = {
                        // Simulate voice note recording toggle
                        onVoiceRecordComplete(14)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Record Voice", tint = Color.White)
                }
            }
        }
    }
}
