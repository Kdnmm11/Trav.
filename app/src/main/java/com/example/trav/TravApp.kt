package com.example.trav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trav.data.AppDatabase
import com.example.trav.ui.screens.MainTabScreen
import com.example.trav.ui.screens.SetupScreen
import com.example.trav.ui.screens.TripListScreen
import com.example.trav.viewmodel.TripViewModel
import com.example.trav.viewmodel.TripViewModelFactory

sealed class ScreenRoute(val route: String) {
    object TripList : ScreenRoute("trip_list")
    object Setup : ScreenRoute("setup")
    object MainTab : ScreenRoute("main_tab/{tripId}") {
        fun createRoute(tripId: Int) = "main_tab/$tripId"
    }
}

@Composable
fun TravApp(database: AppDatabase) {
    val navController = rememberNavController()

    // [확인] 팩토리 인자 2개 (위에서 TripViewModel을 고쳤으므로 이제 에러 안 남)
    val viewModel: TripViewModel = viewModel(
        factory = TripViewModelFactory(database.tripDao(), database.scheduleDao())
    )

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.TripList.route,
        enterTransition = { fadeIn(animationSpec = tween(350)) },
        exitTransition = { fadeOut(animationSpec = tween(350)) },
        popEnterTransition = { fadeIn(animationSpec = tween(350)) },
        popExitTransition = { fadeOut(animationSpec = tween(350)) }
    ) {
        composable(ScreenRoute.TripList.route) {
            // [수정] initial = emptyList() 추가하여 에러 해결
            val tripList by viewModel.tripList.collectAsState(initial = emptyList())

            TripListScreen(
                tripList = tripList,
                onAddClick = { navController.navigate(ScreenRoute.Setup.route) },
                onTripClick = { trip ->
                    navController.navigate(ScreenRoute.MainTab.createRoute(trip.id))
                }
            )
        }

        composable(ScreenRoute.Setup.route) {
            SetupScreen(onSave = { title, start, end ->
                viewModel.addTrip(title, start, end)
                navController.popBackStack()
            })
        }

        composable(ScreenRoute.MainTab.route) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: 0
            val tripFlow = remember(tripId) { viewModel.getTrip(tripId) }

            // [수정] initial = null 추가하여 에러 해결 (Trip은 로딩 중일 수 있으므로 null 허용)
            val trip by tripFlow.collectAsState(initial = null)

            // trip이 로딩되면 화면 표시
            if (trip != null) {
                MainTabScreen(trip = trip)
            }
        }
    }
}