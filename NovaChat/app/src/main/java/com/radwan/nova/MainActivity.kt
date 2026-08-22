package com.radwan.nova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.radwan.nova.ui.navigation.NovaNavGraph
import com.radwan.nova.ui.theme.NovaAccent
import com.radwan.nova.ui.theme.NovaChatTheme
import com.radwan.nova.ui.theme.NovaThemeConfig
import com.radwan.nova.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var themeMode by remember { mutableStateOf(ThemeMode.DARK) }
            var accentColor by remember { mutableStateOf(NovaAccent.BLUE) }

            val themeConfig = NovaThemeConfig(
                themeMode = themeMode,
                accent = accentColor
            )

            NovaChatTheme(config = themeConfig) {
                val navController = rememberNavController()
                NovaNavGraph(
                    navController = navController,
                    currentThemeMode = themeMode,
                    currentAccent = accentColor,
                    onThemeModeChanged = { themeMode = it },
                    onAccentChanged = { accentColor = it }
                )
            }
        }
    }
}
