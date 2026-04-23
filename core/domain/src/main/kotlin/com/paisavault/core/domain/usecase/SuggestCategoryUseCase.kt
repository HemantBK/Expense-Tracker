// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.domain.classifier.ClassifiedCategory
import com.paisavault.core.domain.classifier.MerchantClassifier
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Classifies a merchant string, then resolves the predicted label against available
 * categories. Returns null when no category matches or when confidence is too low.
 */
class SuggestCategoryUseCase @Inject constructor(
    private val classifier: MerchantClassifier,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(merchant: String): Suggestion? {
        if (merchant.isBlank()) return null
        val prediction = classifier.classify(merchant)
        if (!prediction.isUsable) return null
        val categories = categoryRepository.observeAll().first()
        val match = categories.firstOrNull { it.name.equals(prediction.label, ignoreCase = true) }
            ?: return null
        return Suggestion(category = match, confidence = prediction.confidence)
    }

    data class Suggestion(val category: Category, val confidence: Float) {
        val isConfident: Boolean get() = confidence >= ClassifiedCategory.HIGH_CONFIDENCE
    }
}
