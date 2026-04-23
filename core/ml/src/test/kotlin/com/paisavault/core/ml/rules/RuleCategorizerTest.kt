// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.rules

import com.paisavault.core.ml.CategoryPrediction
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.Test

class RuleCategorizerTest {

    private val categorizer = RuleCategorizer()

    @Test
    fun `ZOMATO classified as Food`() = runBlocking {
        val p = categorizer.categorize("ZOMATO")
        p.label shouldBe "Food"
        p.source shouldBe CategoryPrediction.Source.Rule
        p.confidence shouldBeGreaterThan 0.9f
    }

    @Test
    fun `UPI prefix stripped before rule match`() = runBlocking {
        val p = categorizer.categorize("UPI/SWIGGY INSTAMART/7654")
        p.label shouldBe "Food"
    }

    @Test
    fun `Uber is Transport`() = runBlocking {
        categorizer.categorize("UBER INDIA").label shouldBe "Transport"
    }

    @Test
    fun `Amazon is Shopping`() = runBlocking {
        categorizer.categorize("AMAZON").label shouldBe "Shopping"
    }

    @Test
    fun `Jio recharge is Bills`() = runBlocking {
        categorizer.categorize("JIO RECHARGE").label shouldBe "Bills"
    }

    @Test
    fun `Netflix is Entertainment`() = runBlocking {
        categorizer.categorize("NETFLIX").label shouldBe "Entertainment"
    }

    @Test
    fun `Apollo Pharmacy is Health`() = runBlocking {
        categorizer.categorize("APOLLO PHARMACY").label shouldBe "Health"
    }

    @Test
    fun `BigBasket is Groceries`() = runBlocking {
        categorizer.categorize("BIGBASKET").label shouldBe "Groceries"
    }

    @Test
    fun `unknown merchant returns Uncategorized`() = runBlocking {
        val p = categorizer.categorize("XYZ UNKNOWN SHOP 999")
        p.confidence shouldBe 0f
        p.source shouldBe CategoryPrediction.Source.Fallback
    }

    @Test
    fun `blank merchant returns Uncategorized`() = runBlocking {
        categorizer.categorize("") shouldBe CategoryPrediction.Uncategorized
        categorizer.categorize("   ") shouldBe CategoryPrediction.Uncategorized
    }
}
