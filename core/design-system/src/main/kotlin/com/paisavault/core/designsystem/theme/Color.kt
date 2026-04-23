// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val GreenPrimary = Color(0xFF006C4B)
private val GreenPrimaryDark = Color(0xFF7FDAB0)
private val SurfaceLight = Color(0xFFFBFDF9)
private val SurfaceDark = Color(0xFF0F1511)

val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    surface = SurfaceLight,
    onSurface = Color(0xFF1A1C19),
    background = SurfaceLight,
    onBackground = Color(0xFF1A1C19),
)

val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = Color(0xFF003823),
    surface = SurfaceDark,
    onSurface = Color(0xFFE1E3DE),
    background = SurfaceDark,
    onBackground = Color(0xFFE1E3DE),
)
