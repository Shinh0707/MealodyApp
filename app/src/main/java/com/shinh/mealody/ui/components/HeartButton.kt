package com.shinh.mealody.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun HeartButton(
    heartLevel: Int,
    onHeartLevelChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = when (heartLevel) {
        0 -> LocalContentColor.current.copy(alpha = 0.6f)
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFFFF9800)
        else -> Color(0xFFE91E63)
    }

    IconButton(
        onClick = {
            // 次のレベルに更新（0→1→2→3→0→...）
            val nextLevel = (heartLevel + 1) % 4
            onHeartLevelChanged(nextLevel)
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = when (heartLevel) {
                0 -> Icons.Default.FavoriteBorder
                else -> Icons.Default.Favorite
            },
            contentDescription = "お気に入りレベル：$heartLevel",
            tint = tint
        )
    }
}