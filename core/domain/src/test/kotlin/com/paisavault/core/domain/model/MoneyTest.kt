// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class MoneyTest {

    @Test
    fun `add sums minor units of same currency`() {
        val a = Money(minorUnits = 12_550, currency = "INR")
        val b = Money(minorUnits = 450, currency = "INR")

        (a + b) shouldBe Money(minorUnits = 13_000, currency = "INR")
    }

    @Test
    fun `subtract reduces minor units of same currency`() {
        val a = Money(minorUnits = 10_000, currency = "INR")
        val b = Money(minorUnits = 2_500, currency = "INR")

        (a - b) shouldBe Money(minorUnits = 7_500, currency = "INR")
    }

    @Test
    fun `add with mismatched currency throws`() {
        val inr = Money(minorUnits = 1_000, currency = "INR")
        val usd = Money(minorUnits = 1_000, currency = "USD")

        shouldThrow<IllegalArgumentException> { inr + usd }
    }

    @Test
    fun `invalid currency code rejected`() {
        shouldThrow<IllegalArgumentException> { Money(minorUnits = 100, currency = "RUPEE") }
        shouldThrow<IllegalArgumentException> { Money(minorUnits = 100, currency = "IN") }
    }

    @Test
    fun `ZeroInr is zero`() {
        Money.ZeroInr shouldBe Money(minorUnits = 0, currency = "INR")
    }
}
