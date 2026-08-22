package com.radwan.nova.ui.theme

import androidx.compose.ui.graphics.Color

// Neutral Backgrounds
val DarkBackground = Color(0xFF0F141C)
val DarkSurface = Color(0xFF161E2E)
val DarkSurfaceVariant = Color(0xFF1E293B)

val AmoledBackground = Color(0xFF000000)
val AmoledSurface = Color(0xFF0A0A0C)
val AmoledSurfaceVariant = Color(0xFF121216)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)

// Accent Colors Available in NOVA Settings
enum class NovaAccent(val title: String, val primary: Color, val secondary: Color) {
    PURPLE("Purple", Color(0xFF8B5CF6), Color(0xFFA78BFA)),
    BLUE("Blue", Color(0xFF3B82F6), Color(0xFF60A5FA)),
    CYAN("Cyan", Color(0xFF06B6D4), Color(0xFF22D3EE)),
    PINK("Pink", Color(0xFFEC4899), Color(0xFFF472B6)),
    ORANGE("Orange", Color(0xFFF97316), Color(0xFFFB923C)),
    GREEN("Green", Color(0xFF10B981), Color(0xFF34D399))
}

// Status & Message Bubble Colors
val OnlineGreen = Color(0xFF10B981)
val IncomingBubbleDark = Color(0xFF1E293B)
val IncomingBubbleAmoled = Color(0xFF18181B)
val IncomingBubbleLight = Color(0xFFE2E8F0)
