package com.radwan.nova.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radwan.nova.R
import com.radwan.nova.data.models.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionsBottomSheet(
    message: Message,
    onDismiss: () -> Unit,
    onReactionSelect: (String) -> Unit,
    onRemindMeSelect: (Int) -> Unit,
    onPinClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showRemindDialog by remember { mutableStateOf(false) }

    val quickEmojis = listOf("❤️", "👍", "🔥", "😂", "😍", "🎉", "😮", "🙏")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Quick Reactions Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items(quickEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onReactionSelect(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // Message Actions List
            ActionSheetItem(icon = Icons.Outlined.Reply, title = stringResource(R.string.action_reply), onClick = onDismiss)
            ActionSheetItem(
                icon = Icons.Outlined.Alarm,
                title = stringResource(R.string.action_remind),
                onClick = { showRemindDialog = true }
            )
            ActionSheetItem(
                icon = Icons.Outlined.PushPin,
                title = if (message.isPinned) "Unpin" else stringResource(R.string.action_pin),
                onClick = onPinClick
            )
            ActionSheetItem(
                icon = Icons.Outlined.BookmarkBorder,
                title = stringResource(R.string.action_save),
                onClick = onSaveClick
            )
            ActionSheetItem(icon = Icons.Outlined.ContentCopy, title = stringResource(R.string.action_copy), onClick = onDismiss)
            ActionSheetItem(icon = Icons.Outlined.Forward, title = stringResource(R.string.action_forward), onClick = onDismiss)
            ActionSheetItem(
                icon = Icons.Outlined.Delete,
                title = stringResource(R.string.action_delete),
                isDestructive = true,
                onClick = onDeleteClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showRemindDialog) {
        AlertDialog(
            onDismissRequest = { showRemindDialog = false },
            title = { Text(text = stringResource(R.string.action_remind)) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.remind_10_min)) },
                        modifier = Modifier.clickable {
                            onRemindMeSelect(10)
                            showRemindDialog = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.remind_1_hour)) },
                        modifier = Modifier.clickable {
                            onRemindMeSelect(60)
                            showRemindDialog = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.remind_tomorrow)) },
                        modifier = Modifier.clickable {
                            onRemindMeSelect(1440)
                            showRemindDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showRemindDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionSheetItem(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
