// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser

import com.paisavault.data.sms.model.SmsMessage
import javax.inject.Inject

/**
 * Runs the rule registry against an SMS and returns the best parse.
 * See BUILD.md § 25.
 */
class SmsParser @Inject constructor(
    private val rules: Set<@JvmSuppressWildcards SmsParserRule>,
) {
    fun parse(sms: SmsMessage): ParsedTxn? {
        // Input guards — protect against oversized or non-string bodies
        if (sms.body.length > MAX_SMS_BODY_LEN) return null

        val candidates = rules
            .filter { it.matches(sms) }
            .sortedByDescending { it.priority }

        for (rule in candidates) {
            val parsed = rule.parse(sms) ?: continue
            if (parsed.amountMinor <= 0 || parsed.amountMinor > MAX_AMOUNT_MINOR) continue
            if (parsed.merchant.isBlank() || parsed.merchant.length > MAX_MERCHANT_LEN) continue
            return parsed
        }
        return null
    }

    private companion object {
        const val MAX_SMS_BODY_LEN = 2_000
        const val MAX_AMOUNT_MINOR = 100_000_000_000L // 10 crore in paise
        const val MAX_MERCHANT_LEN = 200
    }
}
