// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser.rules

import com.paisavault.core.domain.model.TransactionType
import com.paisavault.data.sms.model.SmsMessage
import com.paisavault.data.sms.parser.ParsedTxn
import com.paisavault.data.sms.parser.RegexHelpers
import com.paisavault.data.sms.parser.SmsParserRule

/**
 * Base class: handles amount extraction + date fallback + merchant cleanup.
 * Subclasses supply a sender regex, debit/credit keyword regexes, and an optional
 * merchant-extraction hook. Covers roughly 80% of real-world Indian bank SMS patterns
 * with tolerance for format drift.
 */
internal abstract class GenericBankRule(
    override val id: String,
    override val bankName: String,
    override val priority: Int,
    private val senderCodes: List<String>,
) : SmsParserRule {

    private val senderRegexes: List<Regex> by lazy { senderCodes.map { RegexHelpers.senderRegex(it) } }

    protected open val debitKeywords = Regex(
        """\b(debited|spent|paid|sent|purchase|withdrawn|txn of|debit of|dr)\b""",
        RegexOption.IGNORE_CASE,
    )
    protected open val creditKeywords = Regex(
        """\b(credited|received|credit of|deposit|cr by|cr\.)\b""",
        RegexOption.IGNORE_CASE,
    )

    /** `at MERCHANT`, `to MERCHANT`, `from MERCHANT`, `Info: UPI/xx/merchant@vpa`. */
    protected open val merchantRegex = Regex(
        """(?:at|to|from|Info:)\s+([A-Z0-9@._\-\s/]{2,60}?)(?:\s+on\b|\.| via | by | UPI[: ]| Ref|$)""",
        RegexOption.IGNORE_CASE,
    )

    /** Date formats like 22-04-26, 22/04/2026, 22Apr26. */
    protected open val dateRegex = Regex(
        """on\s+(\d{1,2}[-/\s]?[A-Za-z0-9]{2,4}[-/\s]?\d{2,4})""",
        RegexOption.IGNORE_CASE,
    )

    /** Bank reference / UPI txn id if present. */
    protected open val referenceRegex = Regex(
        """(?:Txn\s*ID|UPI\s*Ref|Ref\s*No\.?|Ref:|Info:)[:\s]*([A-Z0-9]{6,30})""",
        RegexOption.IGNORE_CASE,
    )

    override fun matches(sms: SmsMessage): Boolean =
        senderRegexes.any { it.containsMatchIn(sms.sender) } &&
            RegexHelpers.AmountRegex.containsMatchIn(sms.body)

    override fun parse(sms: SmsMessage): ParsedTxn? {
        val body = sms.body

        val isDebit = debitKeywords.containsMatchIn(body)
        val isCredit = creditKeywords.containsMatchIn(body)
        if (!isDebit && !isCredit) return null
        val type = if (isDebit) TransactionType.Debit else TransactionType.Credit

        val amountMatch = RegexHelpers.AmountRegex.find(body) ?: return null
        val amountMinor = RegexHelpers.amountToMinor(amountMatch.groupValues[1]) ?: return null

        val merchant = merchantRegex.find(body)?.groupValues?.getOrNull(1)?.trim()
            ?.cleanMerchant() ?: fallbackMerchant(sms)
        if (merchant.isBlank()) return null

        val dateMillis = dateRegex.find(body)?.groupValues?.getOrNull(1)
            ?.let { RegexHelpers.parseDateOrNull(it.replace(" ", "")) }
            ?: sms.receivedAtMillis

        val reference = referenceRegex.find(body)?.groupValues?.getOrNull(1)?.trim()

        return ParsedTxn(
            amountMinor = amountMinor,
            currency = "INR",
            type = type,
            merchant = merchant,
            dateEpochMillis = dateMillis,
            reference = reference,
            bankName = bankName,
            confidence = confidenceFor(reference != null, merchantFromPattern = true),
        )
    }

    protected open fun fallbackMerchant(sms: SmsMessage): String = bankName

    private fun String.cleanMerchant(): String =
        replace(Regex("""\s+"""), " ")
            .replace(Regex("""\b(UPI|POS|NEFT|IMPS|NACH|PAYEE|VPA)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[/_]"""), " ")
            .trim()
            .take(MERCHANT_MAX_LEN)

    private fun confidenceFor(hasReference: Boolean, merchantFromPattern: Boolean): Float =
        when {
            hasReference && merchantFromPattern -> HIGH_CONFIDENCE
            hasReference || merchantFromPattern -> MED_CONFIDENCE
            else -> LOW_CONFIDENCE
        }

    companion object {
        private const val MERCHANT_MAX_LEN = 80
        private const val HIGH_CONFIDENCE = 0.9f
        private const val MED_CONFIDENCE = 0.7f
        private const val LOW_CONFIDENCE = 0.5f
    }
}
