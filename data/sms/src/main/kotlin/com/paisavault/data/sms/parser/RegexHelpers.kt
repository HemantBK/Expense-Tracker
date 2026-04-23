// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Shared regexes and helpers for bank/UPI parser rules. */
internal object RegexHelpers {

    /** Matches sender patterns like `VK-HDFCBK`, `AD-HDFCBK-S`, `JD-HDFCBK`. */
    fun senderRegex(bankCode: String): Regex =
        Regex("""^[A-Z]{2}-${Regex.escape(bankCode)}(-[A-Z0-9]+)?$""", RegexOption.IGNORE_CASE)

    /** Indian amount: `Rs. 1,234.50`, `INR 1234`, `₹ 500`. Capture group 1 = digits. */
    val AmountRegex = Regex(
        """(?:(?:Rs\.?|INR|₹)\s?)([\d,]+(?:\.\d{1,2})?)""",
        RegexOption.IGNORE_CASE,
    )

    /** Date formats commonly used: 22-04-26, 22Apr26, 22-Apr-26, 22/04/26, 22/04/2026. */
    private val dateFormats = listOf(
        "dd-MM-yy", "dd-MM-yyyy", "dd/MM/yy", "dd/MM/yyyy",
        "dd-MMM-yy", "dd-MMM-yyyy", "ddMMMyy", "dd MMM yyyy",
    )

    fun parseDateOrNull(raw: String): Long? {
        val cleaned = raw.trim()
        for (pattern in dateFormats) {
            val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                isLenient = false
                timeZone = TimeZone.getDefault()
            }
            val result = runCatching { sdf.parse(cleaned) }.getOrNull()
            if (result != null) return result.time
        }
        return null
    }

    fun amountToMinor(raw: String): Long? {
        val cleaned = raw.replace(",", "").trim()
        val value = cleaned.toDoubleOrNull() ?: return null
        return (value * MINOR_PER_MAJOR).toLong()
    }

    private const val MINOR_PER_MAJOR = 100.0
}
