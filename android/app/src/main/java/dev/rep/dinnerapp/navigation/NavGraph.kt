package dev.rep.dinnerapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.rep.dinnerapp.ui.MealListScreen
import dev.rep.dinnerapp.ui.PickScreen

object Routes {
    const val PICK = "pick"
    const val MEALS = "meals"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.PICK) {
        composable(Routes.PICK) {
            PickScreen(onOpenMeals = { navController.navigate(Routes.MEALS) })
        }
        composable(Routes.MEALS) {
            MealListScreen(onBack = { navController.popBackStack() })
        }
    }
}
