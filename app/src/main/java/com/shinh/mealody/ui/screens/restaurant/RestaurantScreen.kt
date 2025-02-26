package com.shinh.mealody.ui.screens.restaurant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shinh.mealody.ui.components.ErrorContent
import com.shinh.mealody.ui.components.restaurant.ShopDetailCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantScreen(
    onNavigateBack: () -> Unit,
    viewModel: RestaurantViewModel = hiltViewModel()
) {
    val shop by viewModel.shop.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val favoriteLevel by viewModel.favoriteLevel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val notes by viewModel.notes.collectAsState()

    // エラーメッセージがあればスナックバーに表示
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(shop?.name ?: "店舗詳細") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    // お気に入りボタン
                    IconButton(onClick = {
                        // 現在のレベルが最大なら0に、そうでなければ1つ上げる
                        val newLevel = if (favoriteLevel >= 3) 0.toByte() else (favoriteLevel + 1).toByte()
                        viewModel.updateFavoriteLevel(newLevel)
                    }) {
                        when (favoriteLevel) {
                            0.toByte() -> Icon(Icons.Default.FavoriteBorder, contentDescription = "お気に入りに追加")
                            else -> Icon(
                                Icons.Default.Favorite,
                                contentDescription = "お気に入りレベル$favoriteLevel",
                                // レベルに応じた色を適用できます
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null && shop == null -> {
                    ErrorContent(
                        message = "店舗情報の取得に失敗しました",
                        onRetry = { /* viewModelでの再読み込み処理 */ }
                    )
                }
                shop != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        shop?.let { shopData ->
                            ShopDetailCard(
                                shop = shopData,
                                onFavoriteLevelChanged = { level ->
                                    viewModel.updateFavoriteLevel(level)
                                },
                                notes = notes,
                                onAddShopToNotes = { noteIds ->
                                    viewModel.addShopToNotes(noteIds)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}