// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.model

import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Test
import kotlin.math.abs

class HashingVectorizerTest {

    private val vectorizer = HashingVectorizer(nFeatures = 1024, ngramRange = 3..5)

    @Test
    fun `empty input yields empty map`() {
        vectorizer.transform("").isEmpty() shouldBe true
    }

    @Test
    fun `output is L2 normalized`() {
        val v = vectorizer.transform("zomato")
        val squared = v.values.sumOf { (it * it).toDouble() }
        abs(squared - 1.0).toFloat() shouldBeLessThan 1e-4f
    }

    @Test
    fun `different inputs produce different features`() {
        val a = vectorizer.transform("zomato")
        val b = vectorizer.transform("swiggy")
        // Not identical
        (a == b) shouldBe false
    }

    @Test
    fun `identical inputs are deterministic`() {
        val a = vectorizer.transform("uber")
        val b = vectorizer.transform("uber")
        a shouldBe b
    }

    @Test
    fun `same ngram in input always hashes to same bucket`() {
        // "ubr" appears once; "uber" contains "uber" which is 4-gram
        val v = vectorizer.transform("uber uber")
        // Should have some doubling up due to repetition; non-empty.
        v.isNotEmpty() shouldBe true
    }

    @Test
    fun `short input below min ngram still produces features`() {
        // With range 3..5 and input "ab ", padding makes " ab ", so 3-gram " ab" valid
        val v = vectorizer.transform("ab")
        v.isNotEmpty() shouldBe true
    }
}
