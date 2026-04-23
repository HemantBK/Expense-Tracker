// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.adapter

import com.paisavault.core.domain.classifier.ClassifiedCategory
import com.paisavault.core.domain.classifier.MerchantClassifier
import com.paisavault.core.ml.MerchantCategorizer
import javax.inject.Inject
import javax.inject.Singleton

/** Adapts the internal `MerchantCategorizer` to the domain-facing `MerchantClassifier` port. */
@Singleton
internal class MerchantClassifierAdapter @Inject constructor(
    private val categorizer: MerchantCategorizer,
) : MerchantClassifier {
    override suspend fun classify(merchant: String): ClassifiedCategory {
        val prediction = categorizer.categorize(merchant)
        return ClassifiedCategory(prediction.label, prediction.confidence)
    }
}
