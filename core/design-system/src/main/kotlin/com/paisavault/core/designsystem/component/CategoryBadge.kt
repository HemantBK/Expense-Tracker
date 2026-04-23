// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Colored dot used next to category names in lists and chips. */
@Composable
fun CategoryColorDot(colorArgb: Int, modifier: Modifier = Modifier, diameter: androidx.compose.ui.unit.Dp = 12.dp) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(diameter)
            .background(color = Color(colorArgb), shape = CircleShape),
    )
}

@Composable
fun CategoryChip(name: String, colorArgb: Int, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        CategoryColorDot(colorArgb)
        Text(
            text = name,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
