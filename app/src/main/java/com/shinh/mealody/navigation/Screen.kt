package com.shinh.mealody.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.shinh.mealody.data.model.Area

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object NearbySearch : Screen("nearby_search?lat={lat}&lng={lng}") {
        fun createRoute(lat: Double, lng: Double): String {
            return "nearby_search?lat=$lat&lng=$lng"
        }
    }
    object DetailedSearch : Screen("detailed_search?title={title}") {
        fun createRoute(title: String): String {
            val encodedTitle = Uri.encode(title)
            return "detailed_search?title=$encodedTitle"
        }

        val arguments = listOf(
            navArgument("title") {
                type = NavType.StringType
                defaultValue = Uri.encode("検索結果")
                nullable = true
            }
        )
    }
    object AreaSearch : Screen("area_search/{code}") {
        fun createRoute(area: Area): String {
            return "area_search/${area.code}"
        }
    }

    object Restaurant : Screen("restaurant/{shopId}") {
        fun createRoute(shopId: String): String {
            return "restaurant/$shopId"
        }
    }

    object Note : Screen("note/{noteId}") {
        fun createRoute(noteId: Int): String {
            return "note/$noteId"
        }
    }

}