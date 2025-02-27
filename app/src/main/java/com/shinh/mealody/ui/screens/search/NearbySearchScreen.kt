package com.shinh.mealody.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.ui.components.EmptyContent
import com.shinh.mealody.ui.components.ErrorContent
import com.shinh.mealody.ui.components.LoadingContent
import com.shinh.mealody.ui.components.restaurant.AreaSelection
import com.shinh.mealody.ui.components.restaurant.RestaurantCarousel
import com.shinh.mealody.ui.components.restaurant.ShopDetailCard
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbySearchScreen(
    paddingValues: PaddingValues,
    latLng: LatLng,
    onNavigateToAreaSearch: (Area?) -> Unit,
    viewModel: NearbySearchViewModel = hiltViewModel()
) {
    val nearbyShops by viewModel.nearbyShops.collectAsState()
    val matchingArea by viewModel.matchingArea.collectAsState()
    val currentRange by viewModel.currentRange.collectAsState()
    val searchState by viewModel.searchManager.searchState.collectAsState()
    val selectedShop by viewModel.searchManager.selectedShop.collectAsState()
    val availableMore by viewModel.searchManager.availableMore.collectAsState()
    val storedShops by viewModel.searchManager.storedShops.collectAsState()
    val availableShops by viewModel.searchManager.availableShops.collectAsState()

    LaunchedEffect(latLng) {
        viewModel.initialize(latLng)
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("近くのお店") }
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "検索範囲: ${currentRange.roundToInt()}m",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = currentRange,
                        onValueChange = viewModel::updateRange,
                        valueRange = 300f..3000f,
                        steps = 29
                    )
                }
            }
        }
    ) { innerPadding ->
        when (searchState) {
            SearchState.Initial -> LoadingContent(
                loadingMessage = "準備中"
            )
            SearchState.Loading -> LoadingContent()
            SearchState.Error -> ErrorContent(
                message = "エラーが発生しました",
                onRetry = { viewModel.initialize(latLng) }
            )
            SearchState.Empty -> EmptyContent(
                message = "お店が見つかりませんでした"
            ){
                Text(
                    text = "検索範囲: ${currentRange.roundToInt()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "範囲を広げてみましょう",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            SearchState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Text(
                            text = "近くのお店 ($storedShops/$availableShops)",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        RestaurantCarousel(
                            shops = nearbyShops,
                            currentLocation = latLng,
                            onShopSelected = { viewModel.selectShop(it) },
                            availableMore = availableMore,
                            onShowMore = { viewModel.searchMore() },
                            getFavLevel = {viewModel.getFavLevel(it)},
                            onHeartLevelChanged = { shopId, level ->
                                viewModel.updateFavoriteLevel(shopId, level)
                            }
                        )
                    }

                    item {
                        AnimatedVisibility(
                            visible = selectedShop != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            selectedShop?.let { shop ->
                                ShopDetailCard(
                                    shop = shop,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    favoriteLevel = viewModel.getFavLevel(shop).toByte(),
                                    onFavoriteLevelChanged = { level ->
                                        viewModel.updateFavoriteLevel(shop.id, level.toInt())
                                    },
                                    notes = viewModel.notes.collectAsState().value,
                                    onAddShopToNotes = { noteIds ->
                                        viewModel.addShopToNotes(shop.id, noteIds)
                                    }
                                )
                            }
                        }
                    }

                    item {
                        AreaSelection(
                            matchingArea = matchingArea,
                            onNavigateToAreaSearch = onNavigateToAreaSearch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
