// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Currency-aware money. Stored as minor units (paise for INR, cents for USD) to avoid
 * floating-point error. A Money of ₹125.50 is represented as (minorUnits=12550, currency="INR").
 */
@Serializable
data class Money(
    val minorUnits: Long,
    val currency: String = "INR",
) {
    init {
        require(currency.length == 3) { "currency must be ISO 4217 3-letter code, got '$currency'" }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "cannot add $currency to ${other.currency}" }
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "cannot subtract ${other.currency} from $currency" }
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    companion object {
        val ZeroInr = Money(0, "INR")
    }
}
