// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.rules

import com.paisavault.core.ml.CategoryPrediction
import com.paisavault.core.ml.MerchantCategorizer
import com.paisavault.core.ml.MerchantPreprocessor
import javax.inject.Inject

/**
 * Fast-path categorizer: maps well-known Indian merchant patterns directly to categories.
 * Runs in microseconds. Returns [CategoryPrediction.Uncategorized] for misses so the
 * composite categorizer can try the ML model next. See BUILD.md § 26.
 */
internal class RuleCategorizer @Inject constructor() : MerchantCategorizer {

    private val rules: List<Rule> = listOf(
        Rule(Regex("(?i)\\b(zomato|swiggy|dominos|mcdonald|kfc|pizza|burger|cafe|restaurant|dhaba)\\b"), "Food"),
        Rule(Regex("(?i)\\b(uber|ola|rapido|metro|irctc|redbus|makemytrip|goibibo|indigo|spicejet|vistara|oyo|rail)\\b"), "Transport"),
        Rule(Regex("(?i)\\b(amazon|flipkart|myntra|ajio|meesho|nykaa|tatacliq|snapdeal|shopclues|lenskart|pepperfry)\\b"), "Shopping"),
        Rule(Regex("(?i)\\b(jio|airtel|vi\\b|bsnl|vodafone|tata\\s?power|adani|electricity|water|gas|lpg|broadband|wifi|torrent|mtnl)\\b"), "Bills"),
        Rule(Regex("(?i)\\b(netflix|spotify|hotstar|prime\\s?video|sony\\s?liv|zee5|apple\\s?music|youtube\\s?premium|bookmyshow|pvr|inox)\\b"), "Entertainment"),
        Rule(Regex("(?i)\\b(apollo|medplus|pharmeasy|netmeds|1mg|cipla|hospital|clinic|doctor|diagnost|pathlab|max\\s?health)\\b"), "Health"),
        Rule(Regex("(?i)\\b(bigbasket|grofers|blinkit|zepto|dmart|reliance\\s?fresh|nature'?s\\s?basket|spencer)\\b"), "Groceries"),
    )

    override suspend fun categorize(merchant: String): CategoryPrediction {
        val normalized = MerchantPreprocessor.normalize(merchant)
        if (normalized.isBlank()) return CategoryPrediction.Uncategorized
        val match = rules.firstOrNull { it.pattern.containsMatchIn(normalized) }
            ?: return CategoryPrediction.Uncategorized
        return CategoryPrediction(
            label = match.label,
            confidence = RULE_CONFIDENCE,
            source = CategoryPrediction.Source.Rule,
        )
    }

    private data class Rule(val pattern: Regex, val label: String)

    private companion object {
        const val RULE_CONFIDENCE = 0.95f
    }
}
