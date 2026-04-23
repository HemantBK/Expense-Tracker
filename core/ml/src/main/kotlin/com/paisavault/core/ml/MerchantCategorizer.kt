// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml

/**
 * On-device merchant classifier. Implementations may use rules, a small trained model,
 * or a combination. All calls must complete in well under 50 ms per input. See BUILD.md § 26.
 */
fun interface MerchantCategorizer {
    suspend fun categorize(merchant: String): CategoryPrediction
}
