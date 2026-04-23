// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.designsystem.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Locale-aware money renderer. Amount supplied in minor units. */
@Composable
fun MoneyText(
    amountMinor: Long,
    currencyCode: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = LocalContentColor.current,
    locale: Locale = Locale.getDefault(),
) {
    val formatted = remember(amountMinor, currencyCode, locale) {
        formatMoney(amountMinor, currencyCode, locale)
    }
    Text(text = formatted, modifier = modifier, style = style, color = color)
}

fun formatMoney(amountMinor: Long, currencyCode: String, locale: Locale = Locale.getDefault()): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
    val formatter = NumberFormat.getCurrencyInstance(locale).apply {
        if (currency != null) this.currency = currency
    }
    val fractionDigits = currency?.defaultFractionDigits ?: DEFAULT_FRACTION_DIGITS
    val divisor = pow10(fractionDigits)
    val major = amountMinor.toDouble() / divisor
    return formatter.format(major)
}

private fun pow10(n: Int): Long {
    var r = 1L
    repeat(n) { r *= 10 }
    return r
}

private const val DEFAULT_FRACTION_DIGITS = 2
