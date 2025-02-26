package com.shinh.mealody.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSearchBar(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !expanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = { onExpandedChange(true) }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "検索"
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(
                expandFrom = Alignment.End
            ) + fadeIn(),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.End
            ) + fadeOut()
        ) {
            // TODO: 新しいSearchBarの使用方法で使う
        }
    }
}