package com.shinh.mealody.ui.screens.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shinh.mealody.data.database.entity.ShopEntity
import com.shinh.mealody.ui.components.EmptyContent
import com.shinh.mealody.ui.components.ErrorContent
import com.shinh.mealody.ui.components.restaurant.RestaurantCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    noteId: Int,
    onClickRestaurant: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val note by viewModel.note.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val editingName by viewModel.editingName.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isFavoriteNote by viewModel.isFavoriteNote.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRemoveDialog by remember { mutableStateOf<ShopEntity?>(null) }

    // エラーメッセージがあればスナックバーに表示
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it)
        }
    }

    // 削除確認ダイアログ
    showRemoveDialog?.let { shop ->
        AlertDialog(
            onDismissRequest = { showRemoveDialog = null },
            title = { Text("お店をノートから削除") },
            text = { Text("「${shop.name}」をこのノートから削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeShopFromNote(shop.id)
                        showRemoveDialog = null
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = null }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editingName,
                            onValueChange = {
                                // お気に入りノートの場合は編集不可
                                if (!isFavoriteNote) viewModel.updateNoteName(it)
                            },
                            label = { Text("ノート名") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            // お気に入りノートの場合は編集不可
                            enabled = !isFavoriteNote
                        )
                    } else {
                        Text(note?.name ?: "ノート")
                    }
                },
                navigationIcon = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.cancelEditing() }) {
                            Icon(Icons.Default.Clear, contentDescription = "キャンセル")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                        }
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { viewModel.saveNoteName() }) {
                            Icon(Icons.Default.Check, contentDescription = "保存")
                        }
                    } else {
                        // お気に入りノートでなければ編集ボタンを表示
                        if (!isFavoriteNote) {
                            IconButton(onClick = { viewModel.startEditing() }) {
                                Icon(Icons.Default.Edit, contentDescription = "編集")
                            }
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
                errorMessage != null && note == null -> {
                    ErrorContent(
                        message = "ノート情報の取得に失敗しました",
                        onRetry = { /* viewModelで再読み込み処理 */ }
                    )
                }
                shops.isEmpty() -> {
                    EmptyContent(
                        message = "このノートにはまだお店が登録されていません"
                    ) {
                        Text(
                            text = "お店を検索してノートに追加してみましょう",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(shops) { shop ->
                            ShopItemWithRemove(
                                shop = shop,
                                onClick = { onClickRestaurant(shop.id) },
                                onRemove = { showRemoveDialog = shop }
                            )
                        }

                        // 下部のスペース
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShopItemWithRemove(
    shop: ShopEntity,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Shop情報
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = shop.name,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "ジャンル: ${getGenreName(shop.genreCode)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 削除ボタン
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "ノートから削除",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ジャンルコードから名前を取得する関数（実際の実装ではAPIやリポジトリから取得する）
fun getGenreName(genreCode: String): String {
    // 簡易的な実装（実際にはもっと詳細なマッピングが必要）
    return when (genreCode) {
        "G001" -> "居酒屋"
        "G002" -> "ダイニングバー・バル"
        "G003" -> "創作料理"
        "G004" -> "和食"
        "G005" -> "洋食"
        "G006" -> "イタリアン・フレンチ"
        "G007" -> "中華"
        "G008" -> "焼肉・ホルモン"
        "G009" -> "アジア・エスニック料理"
        "G010" -> "各国料理"
        "G011" -> "カラオケ・パーティ"
        "G012" -> "バー・カクテル"
        "G013" -> "ラーメン"
        "G014" -> "カフェ・スイーツ"
        "G015" -> "その他グルメ"
        "G016" -> "お好み焼き・もんじゃ"
        "G017" -> "韓国料理"
        else -> "その他"
    }
}