package com.shinh.mealody.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.navigation.Screen
import com.shinh.mealody.ui.screens.area.AreaSearchScreen
import com.shinh.mealody.ui.screens.home.HomeScreen
import com.shinh.mealody.ui.screens.library.LibraryScreen
import com.shinh.mealody.ui.screens.note.NoteScreen
import com.shinh.mealody.ui.screens.restaurant.RestaurantScreen
import com.shinh.mealody.ui.screens.search.DetailedSearchScreen
import com.shinh.mealody.ui.screens.search.NearbySearchScreen
import com.shinh.mealody.ui.screens.search.SearchScreen
import com.shinh.mealody.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    fun onNavigateArea(area: Area?){
        area?.let{navController.navigate(Screen.AreaSearch.createRoute(it))}
    }

    fun onNavigateToRestaurant(shopId: String) {
        navController.navigate(Screen.Restaurant.createRoute(shopId))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, "設定")
                        }

                        IconButton(onClick = { /* ユーザープロフィール画面へ */ }) {
                            Icon(Icons.Default.Person, "プロフィール")
                        }
                    }
                }
            )
        },
        bottomBar = {
            MealodyBottomNavigation(navController = navController, items = listOf(
                NavigationItem(
                    title = "Home",
                    icon = Icons.Filled.Home,
                    route = Screen.Home.route
                ),
                NavigationItem(
                    title = "Search",
                    icon = Icons.Filled.Search,
                    route = Screen.Search.route
                ),
                NavigationItem(
                    title = "Library",
                    icon = Icons.Filled.DateRange,
                    route = Screen.Library.route
                )
            ))
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    paddingValues = paddingValues,
                    onNavigateToAreaSearch = ::onNavigateArea,
                    onSearchByCurrentLocation = { latLng ->
                        navController.navigate(Screen.NearbySearch.createRoute(
                            lat = latLng.latitude,
                            lng = latLng.longitude
                        ))
                    },
                    onRestaurantClicked = { shop ->
                        onNavigateToRestaurant(shop.id)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    paddingValues = paddingValues,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.NearbySearch.route,
                arguments = listOf(
                    navArgument("lat") {
                        type = NavType.StringType
                        defaultValue = "0.0"
                    },
                    navArgument("lng") {
                        type = NavType.StringType
                        defaultValue = "0.0"
                    }
                )
            ) { backStackEntry ->
                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
                NearbySearchScreen(
                    paddingValues = paddingValues,
                    latLng = LatLng(lat,lng),
                    onNavigateToAreaSearch = { area ->
                        area?.let {
                            navController.navigate(Screen.AreaSearch.createRoute(it))
                        }
                    }
                )
            }

            composable(
                route = Screen.DetailedSearch.route,
                arguments = Screen.DetailedSearch.arguments
            ) { backStackEntry ->
                val encodedTitle = backStackEntry.arguments?.getString("title")
                val title = encodedTitle?.let{Uri.decode(it)} ?: "検索結果"
                DetailedSearchScreen(
                    paddingValues = paddingValues,
                    title = title,
                    currentLocation = null,
                    onNavigateToAreaSearch = ::onNavigateArea
                )
            }

            composable(
                route = Screen.AreaSearch.route,
                arguments = listOf(
                    navArgument("code") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val areaCode = backStackEntry.arguments?.getString("code") ?: ""
                AreaSearchScreen(
                    areaCode = areaCode,
                    onNavigateToAreaSearch = ::onNavigateArea,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    paddingValues = paddingValues,
                    onSearch = { query ->
                        navController.navigate(Screen.DetailedSearch.createRoute("検索結果"))
                    }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    paddingValues = paddingValues,
                    onClickNote = { noteId ->
                        navController.navigate(Screen.Note.createRoute(noteId))
                    }
                )
            }

            composable(
                route = Screen.Restaurant.route,
                arguments = listOf(
                    navArgument("shopId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val shopId = backStackEntry.arguments?.getString("shopId") ?: ""
                RestaurantScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Note.route,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                NoteScreen(
                    noteId = noteId,
                    onClickRestaurant = ::onNavigateToRestaurant,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun MealodyBottomNavigation(navController: NavHostController, items: List<NavigationItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)