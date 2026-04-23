// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml

import io.kotest.matchers.shouldBe
import org.junit.Test

class MerchantPreprocessorTest {

    @Test
    fun `strips UPI prefix and digit run`() {
        MerchantPreprocessor.normalize("UPI/ZOMATO/FOOD-1234") shouldBe "zomato food"
    }

    @Test
    fun `strips POS prefix`() {
        MerchantPreprocessor.normalize("POS-SWIGGY") shouldBe "swiggy"
    }

    @Test
    fun `lowercases and collapses whitespace`() {
        MerchantPreprocessor.normalize("  ZOMATO   KITCHEN  ") shouldBe "zomato kitchen"
    }

    @Test
    fun `strips VPA punctuation`() {
        MerchantPreprocessor.normalize("VPA:merchant@ybl") shouldBe "merchant ybl"
    }

    @Test
    fun `blank yields blank`() {
        MerchantPreprocessor.normalize("") shouldBe ""
        MerchantPreprocessor.normalize("   ") shouldBe ""
    }

    @Test
    fun `preserves short digits`() {
        // 3+ consecutive digits are stripped, shorter runs survive.
        MerchantPreprocessor.normalize("CAFE 12") shouldBe "cafe 12"
        MerchantPreprocessor.normalize("STORE 1234") shouldBe "store"
    }
}
