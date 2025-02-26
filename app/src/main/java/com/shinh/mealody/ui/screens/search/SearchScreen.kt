package com.shinh.mealody.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.domain.model.SearchQuery
import com.shinh.mealody.ui.theme.RegionColorScheme
import com.shinh.mealody.ui.theme.RegionColors

@Composable
fun SearchScreen(
    paddingValues: PaddingValues,
    viewModel: SearchViewModel = hiltViewModel(),
    onSearch: (SearchQuery) -> Unit = {}
) {
    val largeServiceAreas by viewModel.largeServiceAreas.collectAsState()
    val selectedServiceArea by viewModel.selectedServiceArea.collectAsState()
    val selectedAreaPrefectures by viewModel.selectedAreaPrefectures.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    // 各エリアの選択状態
    val selectedLargeArea by viewModel.selectedLargeArea.collectAsState()
    val selectedMiddleArea by viewModel.selectedMiddleArea.collectAsState()
    val selectedSmallArea by viewModel.selectedSmallArea.collectAsState()

    // 中エリアと小エリアのリスト
    val middleAreas by viewModel.middleAreas.collectAsState()
    val smallAreas by viewModel.smallAreas.collectAsState()

    // 中エリアと小エリアの読み込み状態
    val isMiddleAreasLoading by viewModel.isMiddleAreasLoading.collectAsState()
    val isSmallAreasLoading by viewModel.isSmallAreasLoading.collectAsState()

    // 検索条件のState
    var searchQuery by remember { mutableStateOf(SearchQuery()) }

    // プリセット選択状態
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    val presets = listOf("宴会", "デート", "友人", "家族", "ランチ")

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "エラーが発生しました: $error",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 固定検索ボタン
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f), // 他の要素より前面に表示
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        // 検索クエリが有効かチェック
                        if (searchQuery.isValid()) {
                            viewModel.performSearch(searchQuery) {
                                onSearch(searchQuery) // ここではonSearchはナビゲーションのみを担当
                            }
                        } else {
                            // エラー表示などの処理
                        }
                    },
                    enabled = searchQuery.isValid(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("検索する")
                }
            }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top=64.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 名前・住所・電話番号で検索
            SearchSection(title = "名前・住所・電話番号で調べる") {
                OutlinedTextField(
                    value = searchQuery.name ?: "",
                    onValueChange = {
                        searchQuery = searchQuery.copy(name = it.takeIf { it.isNotEmpty() })
                    },
                    label = { Text("名前") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = searchQuery.tel ?: "",
                    onValueChange = {
                        searchQuery = searchQuery.copy(tel = it.takeIf { it.isNotEmpty() })
                    },
                    label = { Text("電話番号") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = searchQuery.address ?: "",
                    onValueChange = {
                        searchQuery = searchQuery.copy(address = it.takeIf { it.isNotEmpty() })
                    },
                    label = { Text("住所") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 場所から検索
            SearchSection(title = "場所から調べる") {
                // 地図選択ボタン
                OutlinedButton(
                    onClick = { /* TODO: 地図選択 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("地図から選択")
                }

                // 地方セクション
                Text(
                    text = "地方",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 地方（ServiceArea）のカルーセル
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(largeServiceAreas) { area ->
                        val colorScheme = RegionColors.getScheme(area.code)
                        AreaCard(
                            area = area,
                            selected = area.code == selectedServiceArea?.code,
                            colorScheme = colorScheme,
                            onClick = {
                                viewModel.selectServiceArea(area)
                                searchQuery = searchQuery.copy(
                                    largeServiceArea = area.code,
                                    serviceArea = null,
                                    largeArea = null,
                                    middleArea = null,
                                    smallArea = null
                                )
                            }
                        )
                    }
                }

                // 都道府県セクション（ServiceAreaが選択されている場合）
                if (selectedServiceArea != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "都道府県",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val colorScheme = RegionColors.getScheme(selectedServiceArea?.code)

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(selectedAreaPrefectures) { area ->
                            AreaCard(
                                area = area,
                                selected = area.code == selectedLargeArea?.code,
                                colorScheme = colorScheme,
                                onClick = {
                                    if (selectedLargeArea?.code != area.code) {
                                        viewModel.selectLargeArea(area)
                                        searchQuery = searchQuery.copy(
                                            largeArea = listOf(area.code),
                                            middleArea = null,
                                            smallArea = null
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // 市区町村セクション（LargeAreaが選択されている場合）
                if (selectedLargeArea != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "市区町村",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val colorScheme = RegionColors.getScheme(selectedServiceArea?.code)

                    if (isMiddleAreasLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(middleAreas) { area ->
                                AreaCard(
                                    area = area,
                                    selected = area.code == selectedMiddleArea?.code,
                                    colorScheme = colorScheme,
                                    onClick = {
                                        if (selectedMiddleArea?.code != area.code) {
                                            viewModel.selectMiddleArea(area)
                                            searchQuery = searchQuery.copy(
                                                middleArea = listOf(area.code),
                                                smallArea = null
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 小エリアセクション（MiddleAreaが選択されている場合）
                if (selectedMiddleArea != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "小エリア",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val colorScheme = RegionColors.getScheme(selectedServiceArea?.code)

                    if (isSmallAreasLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(smallAreas) { area ->
                                AreaCard(
                                    area = area,
                                    selected = area.code == selectedSmallArea?.code,
                                    colorScheme = colorScheme,
                                    onClick = {
                                        if (selectedSmallArea?.code != area.code) {
                                            viewModel.selectSmallArea(area)
                                            searchQuery = searchQuery.copy(
                                                smallArea = listOf(area.code)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // エリアが選択されていれば、詳細条件を表示
            if (selectedServiceArea != null) {
                Spacer(modifier = Modifier.height(16.dp))

                SearchSection(title = "詳細条件") {
                    // プリセット
                    Text(
                        text = "プリセット",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presets) { preset ->
                            FilterChip(
                                selected = preset == selectedPreset,
                                onClick = {
                                    selectedPreset = if (selectedPreset == preset) null else preset
                                    // TODO: プリセットに応じたSearchQueryの更新
                                },
                                label = { Text(preset) }
                            )
                        }
                    }

                    // キーワード
                    Text(
                        text = "キーワード",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery.keyword ?: "",
                        onValueChange = {
                            searchQuery = searchQuery.copy(keyword = it.takeIf { it.isNotEmpty() })
                        },
                        label = { Text("フリーワード") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    // トグル設定（基本設定）
                    SectionDivider(title = "基本設定")

                    ToggleRow {
                        ToggleItem(
                            label = "禁煙席",
                            checked = searchQuery.nonSmoking ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(nonSmoking = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "駐車場",
                            checked = searchQuery.parking ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(parking = it.takeIf { it })
                            }
                        )
                    }

                    ToggleRow {
                        ToggleItem(
                            label = "バリアフリー",
                            checked = searchQuery.barrierFree ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(barrierFree = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "ペット可",
                            checked = searchQuery.pet ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(pet = it.takeIf { it })
                            }
                        )
                    }

                    ToggleRow {
                        ToggleItem(
                            label = "お子様連れOK",
                            checked = searchQuery.child ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(child = it.takeIf { it })
                            }
                        )
                    }

                    // 時間帯
                    SectionDivider(title = "時間帯")

                    ToggleRow {
                        ToggleItem(
                            label = "ランチ",
                            checked = searchQuery.lunch ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(lunch = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "23時以降食事OK",
                            checked = searchQuery.midnightMeal ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(midnightMeal = it.takeIf { it })
                            }
                        )
                    }

                    // 設備
                    SectionDivider(title = "設備")

                    ToggleRow {
                        ToggleItem(
                            label = "個室",
                            checked = searchQuery.privateRoom ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(privateRoom = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "掘りごたつ",
                            checked = searchQuery.horigotatsu ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(horigotatsu = it.takeIf { it })
                            }
                        )
                    }

                    ToggleRow {
                        ToggleItem(
                            label = "座敷",
                            checked = searchQuery.tatami ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(tatami = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "オープンエア",
                            checked = searchQuery.openAir ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(openAir = it.takeIf { it })
                            }
                        )
                    }

                    // 飲食
                    SectionDivider(title = "飲食")

                    ToggleRow {
                        ToggleItem(
                            label = "食べ放題",
                            checked = searchQuery.freeFood ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(freeFood = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "飲み放題",
                            checked = searchQuery.freeDrink ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(freeDrink = it.takeIf { it })
                            }
                        )
                    }

                    Text(
                        text = "ドリンク",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    ToggleRow {
                        ToggleItem(
                            label = "焼酎",
                            checked = searchQuery.shochu ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(shochu = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "日本酒",
                            checked = searchQuery.sake ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(sake = it.takeIf { it })
                            }
                        )
                    }

                    ToggleRow {
                        ToggleItem(
                            label = "ワイン",
                            checked = searchQuery.wine ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(wine = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "カクテル",
                            checked = searchQuery.cocktail ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(cocktail = it.takeIf { it })
                            }
                        )
                    }

                    // 目的
                    SectionDivider(title = "目的")

                    ToggleRow {
                        ToggleItem(
                            label = "貸切",
                            checked = searchQuery.charter ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(charter = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "ウェディング",
                            checked = searchQuery.wedding ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(wedding = it.takeIf { it })
                            }
                        )
                    }

                    // 宴会収容人数
                    Text(
                        text = "宴会収容人数",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val capacity = searchQuery.partyCapacity?.toString() ?: ""

                        OutlinedTextField(
                            value = capacity,
                            onValueChange = {
                                val newValue = it.toIntOrNull()
                                searchQuery = searchQuery.copy(partyCapacity = newValue)
                            },
                            label = { Text("人数") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )

                        IconButton(onClick = {
                            val current = searchQuery.partyCapacity ?: 0
                            if (current > 0) {
                                searchQuery = searchQuery.copy(partyCapacity = current - 1)
                            }
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "減らす")
                        }

                        IconButton(onClick = {
                            val current = searchQuery.partyCapacity ?: 0
                            searchQuery = searchQuery.copy(partyCapacity = current + 1)
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "増やす")
                        }
                    }

                    ToggleRow {
                        ToggleItem(
                            label = "バンド演奏可",
                            checked = searchQuery.band ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(band = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "TVプロジェクター",
                            checked = searchQuery.tv ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(tv = it.takeIf { it })
                            }
                        )
                    }

                    // エンタメ
                    SectionDivider(title = "エンタメ")

                    ToggleRow {
                        ToggleItem(
                            label = "エンタメ設備",
                            checked = searchQuery.equipment ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(equipment = it.takeIf { it })
                            }
                        )
                        ToggleItem(
                            label = "ライブ・ショー",
                            checked = searchQuery.show ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(show = it.takeIf { it })
                            }
                        )
                    }

                    ToggleRow {
                        ToggleItem(
                            label = "カラオケ",
                            checked = searchQuery.karaoke ?: false,
                            onCheckedChange = {
                                searchQuery = searchQuery.copy(karaoke = it.takeIf { it })
                            }
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun SearchSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            content()
        }
    }
}

@Composable
fun AreaCard(
    area: Area,
    selected: Boolean,
    colorScheme: RegionColorScheme?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(), // 背景色は常に標準カラー
        border = if (selected && colorScheme != null) {
            BorderStroke(2.dp, colorScheme.primary) // 選択時は太い枠線
        } else {
            BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)) // 非選択時は薄い枠線
        },
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            Text(
                text = area.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SectionDivider(title: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall
        )
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
    }
}

@Composable
fun ToggleRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }
}

@Composable
fun RowScope.ToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}