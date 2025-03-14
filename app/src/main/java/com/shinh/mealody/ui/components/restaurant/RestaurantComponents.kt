package com.shinh.mealody.ui.components.restaurant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DirectionsTransit
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SmokeFree
import androidx.compose.material.icons.outlined.SmokingRooms
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.shinh.mealody.data.database.entity.NoteEntity
import com.shinh.mealody.data.model.Area
import com.shinh.mealody.data.model.Shop
import com.shinh.mealody.data.repository.MealodyRepository
import com.shinh.mealody.math.calculateDistance
import com.shinh.mealody.ui.components.HeartButton
import com.shinh.mealody.ui.theme.GenreTheme
import kotlin.math.roundToInt

@Composable
fun RestaurantCarousel(
    shops: List<Shop>,
    currentLocation: LatLng?,
    onShopSelected: (Shop) -> Unit,
    availableMore: Boolean,
    onShowMore: () -> Unit,
    getFavLevel: (Shop) -> Int,
    onHeartLevelChanged: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemsPerColumn = 5
    val shopColumns = shops.chunked(itemsPerColumn)

    val cardHeight = 72.dp
    val spacing = 8.dp
    val totalColumnHeight = (cardHeight * itemsPerColumn) + (spacing * (itemsPerColumn - 1))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(shopColumns) { columnShops ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(280.dp)
            ) {
                columnShops.forEach { shop ->
                    RestaurantCard(
                        shop = shop,
                        favLevel = getFavLevel(shop),
                        currentLocation = currentLocation,
                        onClick = { onShopSelected(shop) },
                        onHeartLevelChanged = { onHeartLevelChanged(shop.id, it) }
                    )
                }

                repeat(itemsPerColumn - columnShops.size) {
                    Spacer(modifier = Modifier.height(cardHeight))
                }
            }
        }

        if (availableMore) {
            item {
                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .height(totalColumnHeight)
                        .clickable { onShowMore() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SkipNext,
                                contentDescription = "もっと見る",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "もっと見る",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    shop: Shop,
    favLevel: Int,
    currentLocation: LatLng?,
    onClick: () -> Unit,
    onHeartLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val genreScheme = GenreTheme.getSchemeByName(shop.genre.name)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, genreScheme.primary),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(genreScheme.container)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, genreScheme.primary),
                modifier = Modifier.size(48.dp)
            ) {
                AsyncImage(
                    model = shop.logoImage ?: shop.photo.pc.s,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = shop.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = genreScheme.primary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(16.dp)
                    ) {
                        Text(
                            text = shop.genre.name,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    if (currentLocation != null) {
                        val distance = calculateDistance(
                            currentLocation.latitude, currentLocation.longitude,
                            shop.lat, shop.lng
                        ).roundToInt()

                        Text(
                            text = "${distance}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                shop.access?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HeartButton(
                heartLevel = favLevel,
                onHeartLevelChanged = {
                    onHeartLevelChanged(it)
                }
            )
        }
    }
}

@Composable
fun AreaSelection(
    matchingArea: Area?,
    onNavigateToAreaSearch: (Area?) -> Unit,
    modifier: Modifier = Modifier
) {
    matchingArea?.let { area ->
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "エリアから探す",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                area.getParentAreas(mutableListOf()).forEach { parea->
                    FilledTonalButton(
                        onClick = { onNavigateToAreaSearch(area) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(parea.name)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailCard(
    shop: Shop,
    modifier: Modifier = Modifier,
    favoriteLevel: Byte = 0,
    onFavoriteLevelChanged: (Byte) -> Unit = {},
    notes: List<NoteEntity> = emptyList(),
    onAddShopToNotes: (List<Int>) -> Unit = {}
) {
    val genreScheme = GenreTheme.getSchemeByName(shop.genre.name)

    var showMenu by remember { mutableStateOf(false) }

    var showNoteSelector by remember { mutableStateOf(false) }

    var selectedNoteIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, genreScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shop.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "オプション"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (favoriteLevel > 0)
                                            Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (favoriteLevel > 0) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        if (favoriteLevel > 0) "お気に入りの変更" else "お気に入りに追加"
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                val newLevel = if (favoriteLevel >= 3) 0.toByte() else (favoriteLevel + 1).toByte()
                                onFavoriteLevelChanged(newLevel)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LibraryAdd,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("ノートに追加")
                                }
                            },
                            onClick = {
                                showMenu = false
                                selectedNoteIds = emptyList()
                                showNoteSelector = true
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                shop.photo.pc.l.let { photoUrl ->
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = genreScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = shop.genre.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    shop.genre.catch.let {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                shop.catch.let {
                    if (it.isNotEmpty()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                        ) {
                            Text(
                                text = "「$it」",
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (shop.parking?.contains("あり") == true) {
                    FacilityChip(
                        text = "駐車場あり",
                        icon = Icons.Outlined.LocalParking,
                        description = shop.parking
                    )
                }

                if (shop.barrierFree?.contains("あり") == true) {
                    FacilityChip(
                        text = "バリアフリー",
                        icon = Icons.Outlined.Accessibility,
                        description = shop.barrierFree
                    )
                }

                when {
                    shop.nonSmoking?.contains("全席禁煙") == true ||
                            shop.nonSmoking?.contains("全面禁煙") == true -> {
                        FacilityChip(
                            text = "全席禁煙",
                            icon = Icons.Outlined.SmokeFree,
                            description = shop.nonSmoking
                        )
                    }
                    shop.nonSmoking?.contains("全席喫煙") == true ||
                            shop.nonSmoking?.contains("喫煙可") == true -> {
                        FacilityChip(
                            text = "喫煙可",
                            icon = Icons.Outlined.SmokingRooms,
                            description = shop.nonSmoking
                        )
                    }
                    shop.nonSmoking?.contains("一部禁煙") == true ||
                            shop.nonSmoking?.contains("禁煙席あり") == true -> {
                        FacilityChip(
                            text = "一部禁煙",
                            icon = Icons.Outlined.SmokeFree,
                            description = shop.nonSmoking
                        )
                    }
                }

                if (shop.freeFood?.contains("あり") == true) {
                    FacilityChip(
                        text = "食べ放題",
                        icon = Icons.Outlined.Restaurant,
                        description = shop.freeFood
                    )
                }
                if (shop.freeDrink?.contains("あり") == true) {
                    FacilityChip(
                        text = "飲み放題",
                        icon = Icons.Outlined.LocalBar,
                        description = shop.freeDrink
                    )
                }
                if (shop.privateRoom?.contains("あり") == true) {
                    FacilityChip(
                        text = "個室あり",
                        icon = Icons.Outlined.MeetingRoom,
                        description = shop.privateRoom
                    )
                }
                if (shop.wifi?.contains("あり") == true) {
                    FacilityChip(
                        text = "Wi-Fi",
                        icon = Icons.Outlined.Wifi,
                        description = shop.wifi
                    )
                }

                if (shop.card?.contains("利用可") == true) {
                    FacilityChip(
                        text = "カード可",
                        icon = Icons.Outlined.CreditCard,
                        description = shop.card
                    )
                }

                if ((shop.pet?.contains("可") == true) &&
                    !shop.pet.contains("不可")) {
                    FacilityChip(
                        text = "ペット可",
                        icon = Icons.Outlined.Pets,
                        description = shop.pet
                    )
                }

                if (((shop.child?.contains("可") == true) &&
                            !shop.child.contains("不可")) ||
                    (shop.child?.contains("歓迎") == true)) {
                    FacilityChip(
                        text = "お子様連れOK",
                        icon = Icons.Outlined.ChildCare,
                        description = shop.child
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shop.access?.let {
                    InfoRow(
                        icon = Icons.Outlined.DirectionsTransit,
                        label = "アクセス",
                        value = it
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    shop.capacity?.let {
                        InfoItem(
                            icon = Icons.Outlined.Chair,
                            label = "総席数",
                            value = "$it 席",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    shop.partyCapacity?.let {
                        InfoItem(
                            icon = Icons.Outlined.Groups,
                            label = "最大宴会",
                            value = "$it 名",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                InfoRow(
                    icon = Icons.Outlined.Place,
                    label = "住所",
                    value = shop.address
                )

                shop.open?.let {
                    InfoRow(
                        icon = Icons.Outlined.Schedule,
                        label = "営業時間",
                        value = it
                    )
                }

                shop.close?.let {
                    InfoRow(
                        icon = Icons.Outlined.EventBusy,
                        label = "定休日",
                        value = it
                    )
                }

                if (shop.budget?.name != null && shop.budget.average != "") {
                    InfoRow(
                        icon = Icons.Outlined.Payments,
                        label = "予算",
                        value = "${shop.budget.name} (平均: ${shop.budget.average})"
                    )
                }
            }

            var showDetails by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showDetails = !showDetails },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, genreScheme.primary)
            ) {
                Text(text = if (showDetails) "詳細を閉じる" else "詳細を見る")
            }

            AnimatedVisibility(visible = showDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shop.urls.pc.let {
                        InfoRow(
                            icon = Icons.Outlined.Language,
                            label = "URL",
                            value = it,
                            isLink = true
                        )
                    }
                    shop.course?.let {
                        InfoRow(
                            icon = Icons.Outlined.Restaurant,
                            label = "コース",
                            value = it
                        )
                    }

                    shop.shopDetailMemo?.let {
                        InfoRow(
                            icon = Icons.Outlined.Info,
                            label = "店舗メモ",
                            value = it
                        )
                    }
                    val facilityDetails = buildFacilityList(shop)
                    if (facilityDetails.isNotEmpty()) {
                        Text(
                            text = "設備・特徴",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            facilityDetails.forEach { facility ->
                                FacilityChip(text = facility.first, description = facility.second)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showNoteSelector) {
        ModalBottomSheet(
            onDismissRequest = { showNoteSelector = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "ノートに追加",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (notes.isEmpty()) {
                    Text(
                        text = "ノートがありません。先にノートを作成してください。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(notes) { note ->
                            if (note.id != MealodyRepository.FAVORITES_NOTE_ID) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedNoteIds = if (selectedNoteIds.contains(note.id)) {
                                                selectedNoteIds - note.id
                                            } else {
                                                selectedNoteIds + note.id
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = selectedNoteIds.contains(note.id),
                                        onCheckedChange = { checked ->
                                            selectedNoteIds = if (checked) {
                                                selectedNoteIds + note.id
                                            } else {
                                                selectedNoteIds - note.id
                                            }
                                        }
                                    )

                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(
                                            text = note.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${note.shops.size}件のお店",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showNoteSelector = false }
                    ) {
                        Text("キャンセル")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onAddShopToNotes(selectedNoteIds)
                            showNoteSelector = false
                        },
                        enabled = selectedNoteIds.isNotEmpty()
                    ) {
                        Text("追加")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun buildFacilityList(shop: Shop): List<Pair<String,String>> {
    val list = mutableListOf<Pair<String,String>>()

    if (shop.tatami?.contains("あり") == true) list.add("座敷あり" to shop.tatami)

    if (shop.horigotatsu?.contains("あり") == true) list.add("掘りごたつあり" to shop.horigotatsu)

    if (shop.shopDetailMemo?.contains("カクテル充実") == true) list.add("カクテル充実" to shop.shopDetailMemo)

    if (shop.shopDetailMemo?.contains("焼酎充実") == true) list.add("焼酎充実" to shop.shopDetailMemo)

    if (shop.shopDetailMemo?.contains("日本酒充実") == true) list.add("日本酒充実" to shop.shopDetailMemo)

    if (shop.shopDetailMemo?.contains("ワイン充実") == true) list.add("ワイン充実" to shop.shopDetailMemo)

    if (shop.karaoke?.contains("あり") == true) list.add("カラオケあり" to shop.karaoke)

    if (shop.lunch?.contains("あり") == true) list.add("ランチあり" to shop.lunch)

    if (shop.midnight?.contains("営業している") == true) list.add("23時以降営業" to shop.midnight)

    if (shop.english?.contains("あり") == true) list.add("英語メニューあり" to shop.english)

    if ((shop.charter?.contains("可") == true) && (!shop.charter.contains("不可"))) list.add("貸切可" to shop.charter)

    if (shop.nightView?.contains("あり") == true) list.add("夜景あり" to shop.nightView)

    if (shop.openAir?.contains("あり") == true) list.add("オープンエア" to shop.openAir)

    if (shop.sommelier?.contains("いる") == true) list.add("ソムリエ在籍" to shop.sommelier)

    return list
}

@Composable
fun FacilityChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    description: String? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val hasDescription = !description.isNullOrEmpty()
    val density = LocalDensity.current

    Box(modifier = modifier) {
        val borderModifier = if (hasDescription) {
            Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
        } else {
            Modifier
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .then(borderModifier)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { _ ->
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    )
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isPressed && hasDescription) {
            val offsetInPx = with(density) {
                IntOffset(
                    x = 0.dp.roundToPx(),
                    y = (-50).dp.roundToPx()
                )
            }
            Popup(
                alignment = Alignment.BottomCenter,
                offset = offsetInPx,
                onDismissRequest = {}
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                ) {
                    Text(
                        text = description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isLink: Boolean = false
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}