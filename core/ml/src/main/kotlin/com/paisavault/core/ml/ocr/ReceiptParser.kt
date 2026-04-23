// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.ocr

import javax.inject.Inject

/**
 * Extracts amount + best-effort merchant from free-form OCR output of an Indian
 * cash/card receipt.
 *
 * Strategy:
 *  - Scan lines for an amount prefixed by `TOTAL`, `AMOUNT`, `GRAND TOTAL`, or `Rs/₹/INR`
 *  - Pick the largest such amount as the total (receipts usually list subtotals first)
 *  - Merchant guess = first non-empty line that isn't a date/amount/header word
 *
 * Deliberately conservative — when we can't be confident, return null and let the user
 * fill in the fields manually.
 */
class ReceiptParser @Inject constructor() {

    fun parse(text: String): ParsedReceipt? {
        if (text.isBlank()) return null
        val lines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val amount = extractAmount(lines) ?: return null
        val merchant = extractMerchant(lines)
        return ParsedReceipt(amountMinor = amount, merchant = merchant)
    }

    private fun extractAmount(lines: List<String>): Long? {
        val candidates = mutableListOf<Long>()
        for (line in lines) {
            val priorityMatch = PRIORITY_AMOUNT.find(line)
            if (priorityMatch != null) {
                parseAmount(priorityMatch.groupValues[1])?.let { return it }
            }
            FALLBACK_AMOUNT.findAll(line).forEach { m ->
                parseAmount(m.groupValues[1])?.let { candidates += it }
            }
        }
        return candidates.maxOrNull()
    }

    private fun extractMerchant(lines: List<String>): String? {
        for (line in lines.take(MERCHANT_CANDIDATE_LINES)) {
            if (IS_DATE.containsMatchIn(line)) continue
            if (IS_AMOUNT_ONLY.containsMatchIn(line)) continue
            if (line.length < MIN_MERCHANT_LEN) continue
            if (line.length > MAX_MERCHANT_LEN) continue
            val cleaned = line.replace(Regex("""\s+"""), " ").trim()
            if (cleaned.isNotBlank()) return cleaned
        }
        return null
    }

    private fun parseAmount(raw: String): Long? {
        val cleaned = raw.replace(",", "").trim()
        val value = cleaned.toDoubleOrNull() ?: return null
        if (value <= 0 || value > MAX_AMOUNT) return null
        return (value * MINOR_PER_MAJOR).toLong()
    }

    data class ParsedReceipt(val amountMinor: Long, val merchant: String?)

    private companion object {
        val PRIORITY_AMOUNT = Regex(
            """(?i)(?:grand\s*total|total|amount\s*due|amount|net\s*payable|balance)[:\s]*(?:Rs\.?|INR|₹)?\s*([\d,]+(?:\.\d{1,2})?)""",
        )
        val FALLBACK_AMOUNT = Regex("""(?i)(?:Rs\.?|INR|₹)\s*([\d,]+(?:\.\d{1,2})?)""")
        val IS_DATE = Regex("""\d{1,2}[-/]\d{1,2}[-/]\d{2,4}""")
        val IS_AMOUNT_ONLY = Regex("""^[\d,.\s]+$""")
        const val MERCHANT_CANDIDATE_LINES = 6
        const val MIN_MERCHANT_LEN = 3
        const val MAX_MERCHANT_LEN = 60
        const val MAX_AMOUNT = 10_000_000.0
        const val MINOR_PER_MAJOR = 100.0
    }
}
