package com.shinh.mealody.ui.screens.home

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.model.SmallArea

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    onNavigateToAreaSearch: (Area?) -> Unit,
    onSearchByCurrentLocation: (LatLng) -> Unit,
    onRestaurantClicked: (Shop) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val matchingArea by viewModel.matchingArea.collectAsState()
    val visitedShops by viewModel.visitedShops.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val areGranted = permissions.entries.all { it.value }
        if (areGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeSection(title = "ライブラリ") {
                Text("お気に入りの店舗を表示")
            }

            HomeSection(title = "あなたの地域") {
                LocationCards(
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    matchingArea = matchingArea,
                    onNavigateToAreaSearch = onNavigateToAreaSearch,
                    onSearchByCurrentLocation = {
                        viewModel.onSearchByCurrentLocation(
                            onSearchByCurrentLocation
                        )
                    }
                )
            }

            HomeSection(title = "もう一度") {
                HistoryCarousel(
                    shops = visitedShops,
                    onShopClick = { shop ->
                        Log.d("HomeScreen", "Shop clicked: $shop")
                        onRestaurantClicked(shop)
                    }
                )
            }

            HomeSection(title = "クーポン") {
                CouponCarousel()
            }
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}
@Composable
private fun LocationCards(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    matchingArea: SmallArea?,
    onNavigateToAreaSearch: (Area?) -> Unit,
    onSearchByCurrentLocation: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (!hasLocationPermission) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "現在地を取得すると、より便利にお店を探せます",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = onRequestPermission
                    ) {
                        Text("現在地取得を許可")
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(96.dp),
            onClick = {
                if (hasLocationPermission) {
                    onSearchByCurrentLocation()
                } else {
                    onRequestPermission()
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "近所のお店",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "徒歩圏内のお店をチェック",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        matchingArea?.let { area ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(96.dp),
                onClick = { onNavigateToAreaSearch(area) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "このエリアで探す",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        matchingArea?.parentArea?.let { area ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(96.dp),
                onClick = { onNavigateToAreaSearch(area) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "市区町村から探す",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        matchingArea?.parentArea?.parentArea?.let { area ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(96.dp),
                onClick = { onNavigateToAreaSearch(area) }
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = area.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "都道府県から探す",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun HistoryCarousel(
    shops: List<Shop>,
    onShopClick: (Shop) -> Unit
) {
    if (shops.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "まだ履歴はありません",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(shops) { shop ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .height(160.dp),
                    onClick = { onShopClick(shop) }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AsyncImage(
                            model = shop.photo.pc.s,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = shop.name,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = shop.genre.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponCarousel() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(5) { index ->
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .height(180.dp),
                onClick = { /* TODO: クーポンのクリックハンドリング */ }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "クーポン ${index + 1}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "有効期限: 2024/03/31まで",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}