// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.ocr

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class ReceiptParserTest {

    private val parser = ReceiptParser()

    @Test
    fun `parses typical restaurant receipt`() {
        val text = """
            SPICE GARDEN
            123 MG Road, Bengaluru
            Date: 22-04-2026

            Subtotal      Rs 450.00
            GST 5%        Rs 22.50

            TOTAL         Rs 472.50
        """.trimIndent()

        val parsed = parser.parse(text)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 47_250L // Rs 472.50 in paise
        parsed.merchant shouldBe "SPICE GARDEN"
    }

    @Test
    fun `uses largest amount when priority marker absent`() {
        val text = """
            SOME STORE
            Rs 50
            Rs 100
            Rs 200
        """.trimIndent()

        val parsed = parser.parse(text)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 20_000L
    }

    @Test
    fun `picks GRAND TOTAL over intermediate totals`() {
        val text = """
            SHOP ABC
            Subtotal Rs 1000
            Total Rs 1100
            GRAND TOTAL Rs 1180.50
        """.trimIndent()

        val parsed = parser.parse(text)
        parsed!!.amountMinor shouldBe 118_050L
    }

    @Test
    fun `returns null on empty input`() {
        parser.parse("") shouldBe null
        parser.parse("   ") shouldBe null
    }

    @Test
    fun `returns null when no amount found`() {
        parser.parse("HELLO WORLD\nNo prices here\nJust text") shouldBe null
    }

    @Test
    fun `rejects unreasonably large amounts`() {
        parser.parse("Rs 999999999.99") shouldBe null
    }

    @Test
    fun `handles comma thousands separator`() {
        val parsed = parser.parse("SHOP\nTOTAL Rs 1,250.75")
        parsed!!.amountMinor shouldBe 125_075L
    }
}
