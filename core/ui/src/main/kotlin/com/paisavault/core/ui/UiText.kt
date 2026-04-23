// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

/**
 * UI-layer-agnostic string wrapper. Domain errors map to [UiText], which is resolved
 * against resources at composition time. See BUILD.md § 15.3.
 */
sealed interface UiText {
    data class Literal(val value: String) : UiText
    data class StringResource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is Literal -> value
        is StringResource -> stringResource(id, *args.toTypedArray())
    }

    fun resolve(context: Context): String = when (this) {
        is Literal -> value
        is StringResource -> context.getString(id, *args.toTypedArray())
    }
}
