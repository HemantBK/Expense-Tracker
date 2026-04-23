// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.classifier

/**
 * Domain-level port for on-device merchant classification. Implementation lives in
 * `:core:ml`. Keeps feature and data modules decoupled from ML internals.
 */
fun interface MerchantClassifier {
    suspend fun classify(merchant: String): ClassifiedCategory
}

data class ClassifiedCategory(
    val label: String,
    val confidence: Float,
) {
    val isConfident: Boolean get() = confidence >= HIGH_CONFIDENCE
    val isUsable: Boolean get() = confidence >= MIN_USABLE_CONFIDENCE

    companion object {
        const val HIGH_CONFIDENCE = 0.85f
        const val MIN_USABLE_CONFIDENCE = 0.4f
        val Uncategorized = ClassifiedCategory(label = "Other", confidence = 0f)
    }
}
