package com.radwan.nova.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

data class NovaThemeConfig(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accent: NovaAccent = NovaAccent.BLUE
)

val LocalNovaThemeConfig = staticCompositionLocalOf { NovaThemeConfig() }

@Composable
fun NovaChatTheme(
    config: NovaThemeConfig = NovaThemeConfig(),
    content: @Composable () -> Unit
) {
    val isDark = when (config.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val isAmoled = config.themeMode == ThemeMode.AMOLED

    val background = when {
        isAmoled -> AmoledBackground
        isDark -> DarkBackground
        else -> LightBackground
    }

    val surface = when {
        isAmoled -> AmoledSurface
        isDark -> DarkSurface
        else -> LightSurface
    }

    val surfaceVariant = when {
        isAmoled -> AmoledSurfaceVariant
        isDark -> DarkSurfaceVariant
        else -> LightSurfaceVariant
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = config.accent.primary,
            secondary = config.accent.secondary,
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFF94A3B8)
        )
    } else {
        lightColorScheme(
            primary = config.accent.primary,
            secondary = config.accent.secondary,
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
            onPrimary = Color.White,
            onBackground = Color(0xFF0F172A),
            onSurface = Color(0xFF0F172A),
            onSurfaceVariant = Color(0xFF64748B)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = background.toArgb()
            window.navigationBarColor = background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalNovaThemeConfig provides config) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NovaTypography,
            content = content
        )
    }
}
