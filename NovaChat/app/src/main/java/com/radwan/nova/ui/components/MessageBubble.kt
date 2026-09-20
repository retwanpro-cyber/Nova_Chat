package com.radwan.nova.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radwan.nova.data.models.Message
import com.radwan.nova.data.models.MessageType
import com.radwan.nova.ui.theme.IncomingBubbleDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isSender: Boolean,
    onLongClick: () -> Unit
) {
    val bubbleColor = if (isSender) MaterialTheme.colorScheme.primary else IncomingBubbleDark
    val textColor = if (isSender) Color.White else MaterialTheme.colorScheme.onSurface
    val shape = if (isSender) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = if (isSender) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(shape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = { /* Tap message */ },
                    onLongClick = onLongClick
                )
                .padding(12.dp)
        ) {
            Column {
                // Image Attachment Message
                if (message.messageType == MessageType.IMAGE && message.attachmentUrl != null) {
                    AsyncImage(
                        model = message.attachmentUrl,
                        contentDescription = "Image attachment",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Voice Message Bubble
                if (message.messageType == MessageType.VOICE) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { /* Play/Pause Audio */ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Waveform Placeholder
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(20) { index ->
                                val height = (10 + (index * 7) % 24).dp
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(height)
                                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "0:${message.durationSeconds ?: 14}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Text Message Body
                if (!message.text.isNullOrBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp & Read Receipts
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMessageTime(message.timestamp),
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (isSender) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            contentDescription = "Status",
                            tint = if (message.isRead) Color(0xFF60A5FA) else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Display Reactions below bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(top = 2.dp, start = 4.dp, end = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                message.reactions.forEach { reaction ->
                    Text(text = reaction.emoji, fontSize = 13.sp)
                }
            }
        }

        // Display Reminder Banner if set
        if (message.reminder != null) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = "Reminder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = message.reminder.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun formatMessageTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val str = raw.trim()
    return try {
        val num = str.toLongOrNull()
        if (num != null) {
            val d = java.util.Date(num)
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getDefault()
            sdf.format(d)
        } else {
            // محاولة التحليل عبر عدة صيغ شائعة
            val patterns = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "HH:mm:ss",
                "HH:mm"
            )
            var parsedDate: java.util.Date? = null
            for (p in patterns) {
                try {
                    val sdf = java.text.SimpleDateFormat(p, java.util.Locale.US)
                    if (str.contains("Z") || str.contains("+")) {
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }
                    val d = sdf.parse(str)
                    if (d != null) {
                        parsedDate = d
                        break
                    }
                } catch (e: Exception) {}
            }
            if (parsedDate != null) {
                val outSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                outSdf.timeZone = java.util.TimeZone.getDefault()
                outSdf.format(parsedDate)
            } else {
                // استخراج الساعة والدقيقة بنمط Regex في حال فشل أي تحليل قياسي
                val match = Regex("(\\d{1,2}):(\\d{2})").find(str)
                if (match != null) {
                    val h = match.groupValues[1].toIntOrNull() ?: 0
                    val m = match.groupValues[2]
                    val amPm = if (h >= 12) (if (java.util.Locale.getDefault().language == "ar") "م" else "PM") else (if (java.util.Locale.getDefault().language == "ar") "ص" else "AM")
                    val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
                    "$displayH:$m $amPm"
                } else {
                    str
                }
            }
        }
    } catch (e: Exception) {
        str
    }
}
