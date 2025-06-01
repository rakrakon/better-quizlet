package com.rakra.wordsprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rakra.wordsprint.ui.theme.WordSprintTheme

val ALEF_FONT = FontFamily(
    Font(R.font.alef_regular, FontWeight.Normal)
)

enum class Screen {
    Welcome,
    UnitSelection
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordSprintTheme {
                var currentScreen by remember { mutableStateOf(Screen.Welcome) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Welcome -> WelcomeScreen {
                                currentScreen = Screen.UnitSelection
                            }

                            Screen.UnitSelection -> UnitsSelectScreen()
                        }
                    }
                }
            }
        }
    }
}
