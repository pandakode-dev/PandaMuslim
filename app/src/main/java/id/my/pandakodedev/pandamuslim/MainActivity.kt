package id.my.pandakodedev.pandamuslim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PandaMuslimAppNavigator()
                }
            }
        }
    }
}

@Composable
fun PandaMuslimAppNavigator() {
    var currentScreen by remember { mutableStateOf("HOME") }

    BackHandler(enabled = currentScreen != "HOME") {
        currentScreen = "HOME"
    }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            "HOME" -> {
                PandaMuslimHomeScreen(
                    onMenuClick = { selectedMenuTitle ->
                        currentScreen = selectedMenuTitle
                    }
                )
            }
            "Al-Quran" -> {
                QuranScreen(
                    onBack = { currentScreen = "HOME" }
                )
            }
            "Asmaul" -> {
                AsmaulHusnaScreen(
                    onBack = { currentScreen = "HOME" }
                )
            }
            "Tasbih" -> {
                Tasbih3DScreen(
                    onBack = { currentScreen = "HOME" }
                )
            }
            else -> {
                val categoryMap = mapOf(
                    "Doa" to "doa",
                    "Tajwid" to "tajwid",
                    "Video" to "video",
                    "Belajar" to "belajar",
                    "Iqro" to "iqro"
                )
                val categoryKey = categoryMap[screen] ?: "belajar"
                DynamicContentScreen(
                    menuTitle = screen,
                    categoryKey = categoryKey,
                    onBack = { currentScreen = "HOME" }
                )
            }
        }
    }
}