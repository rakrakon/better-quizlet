package com.rakra.wordsprint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rakra.wordsprint.database.rememberWordViewModel
import com.rakra.wordsprint.screens.MainPage
import com.rakra.wordsprint.screens.MemorizationScreen
import com.rakra.wordsprint.screens.PracticeSelectScreen
import com.rakra.wordsprint.screens.UnitSelectScreen
import com.rakra.wordsprint.ui.theme.WordSprintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordSprintTheme {
                AppNavHost()
            }
        }
    }

    @Composable
    fun AppNavHost() {
        val navController = rememberNavController()
        val viewModel = rememberWordViewModel();

        NavHost(
            navController = navController,
            startDestination = "main"
        ) {
            composable("main") {
                MainPage(navController)
            }
            composable("unit_selection") {
                UnitSelectScreen(navController)
            }
            composable(
                route = "unit_screen/{unitNumber}",
                arguments = listOf(navArgument("unitNumber") { type = NavType.IntType })
            ) { backStackEntry ->
                val unitNumber = backStackEntry.arguments?.getInt("unitNumber") ?: 1

                // TODO: Make This actually work :[
                val practiceStates = remember { List(10) { false } }

                PracticeSelectScreen(
                    navController = navController,
                    unit = unitNumber,
                    practiceStates = practiceStates,
                )
            }
            composable(
                route = "memorization/{unit}",
                arguments = listOf(navArgument("unit") { type = NavType.IntType })
            ) { backStackEntry ->
                val unit = backStackEntry.arguments?.getInt("unit") ?: 0
                MemorizationScreen(
                    navController = navController,
                    unit = unit,
                    viewModel = viewModel
                )
            }
        }
    }
}
