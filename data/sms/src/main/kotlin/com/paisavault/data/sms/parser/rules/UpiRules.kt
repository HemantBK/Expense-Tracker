// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser.rules

import com.paisavault.core.domain.model.TransactionType
import com.paisavault.data.sms.model.SmsMessage
import com.paisavault.data.sms.parser.ParsedTxn
import com.paisavault.data.sms.parser.RegexHelpers
import com.paisavault.data.sms.parser.SmsParserRule
import javax.inject.Inject

/** PhonePe / Google Pay / Paytm have distinctive patterns like "You paid Rs X to Y". */
internal abstract class UpiAppRule(
    override val id: String,
    override val bankName: String,
    override val priority: Int,
    private val senderCodes: List<String>,
    private val paidRegex: Regex,
    private val receivedRegex: Regex,
) : SmsParserRule {

    private val senderRegexes by lazy { senderCodes.map { RegexHelpers.senderRegex(it) } }
    private val referenceRegex = Regex(
        """(?:Txn\s*ID|Transaction\s*ID|UPI\s*ref|Order\s*ID)[:\s]*([A-Z0-9]{6,30})""",
        RegexOption.IGNORE_CASE,
    )

    override fun matches(sms: SmsMessage): Boolean =
        senderRegexes.any { it.containsMatchIn(sms.sender) } &&
            (paidRegex.containsMatchIn(sms.body) || receivedRegex.containsMatchIn(sms.body))

    override fun parse(sms: SmsMessage): ParsedTxn? {
        val body = sms.body
        val paid = paidRegex.find(body)
        val received = receivedRegex.find(body)
        val type: TransactionType
        val merchant: String
        val amountRaw: String
        when {
            paid != null -> {
                type = TransactionType.Debit
                amountRaw = paid.groupValues[AMOUNT_GROUP]
                merchant = paid.groupValues[MERCHANT_GROUP].trim()
            }
            received != null -> {
                type = TransactionType.Credit
                amountRaw = received.groupValues[AMOUNT_GROUP]
                merchant = received.groupValues[MERCHANT_GROUP].trim()
            }
            else -> return null
        }
        if (merchant.isBlank()) return null
        val amountMinor = RegexHelpers.amountToMinor(amountRaw) ?: return null
        val reference = referenceRegex.find(body)?.groupValues?.getOrNull(1)

        return ParsedTxn(
            amountMinor = amountMinor,
            currency = "INR",
            type = type,
            merchant = merchant.take(MERCHANT_MAX_LEN),
            dateEpochMillis = sms.receivedAtMillis,
            reference = reference,
            bankName = bankName,
            confidence = if (reference != null) HIGH_CONFIDENCE else MED_CONFIDENCE,
        )
    }

    companion object {
        const val AMOUNT_GROUP = 1
        const val MERCHANT_GROUP = 2
        const val MERCHANT_MAX_LEN = 80
        const val HIGH_CONFIDENCE = 0.95f
        const val MED_CONFIDENCE = 0.8f
    }
}

internal class PhonePeRule @Inject constructor() : UpiAppRule(
    id = "phonepe",
    bankName = "PhonePe",
    priority = 90,
    senderCodes = listOf("PHONPE", "PHNPE"),
    paidRegex = Regex(
        """You\s+paid\s+(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z0-9@._\-\s]{2,60}?)(?:\s+via|\s+on|\s+\.|\s*$)""",
        RegexOption.IGNORE_CASE,
    ),
    receivedRegex = Regex(
        """You\s+received\s+(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z0-9@._\-\s]{2,60}?)(?:\s+via|\s+on|\.|$)""",
        RegexOption.IGNORE_CASE,
    ),
)

internal class GPayRule @Inject constructor() : UpiAppRule(
    id = "gpay",
    bankName = "Google Pay",
    priority = 90,
    senderCodes = listOf("GOOGPY", "GPAY"),
    paidRegex = Regex(
        """You\s+paid\s+(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z0-9@._\-\s]{2,60}?)(?:\s+via|\s+on|\.|$)""",
        RegexOption.IGNORE_CASE,
    ),
    receivedRegex = Regex(
        """You\s+received\s+(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z0-9@._\-\s]{2,60}?)(?:\s+via|\s+on|\.|$)""",
        RegexOption.IGNORE_CASE,
    ),
)

internal class PaytmRule @Inject constructor() : UpiAppRule(
    id = "paytm",
    bankName = "Paytm",
    priority = 90,
    senderCodes = listOf("PAYTM", "PAYTMB"),
    paidRegex = Regex(
        """(?:You\s+have\s+successfully\s+paid|You\s+paid)\s+(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)\s+to\s+([A-Za-z0-9@._\-\s]{2,60}?)(?:\s+from|\s+via|\.|$)""",
        RegexOption.IGNORE_CASE,
    ),
    receivedRegex = Regex(
        """You\s+(?:have\s+)?received\s+(?:Rs\.?|INR|₹)\s?([\d,]+(?:\.\d{1,2})?)\s+from\s+([A-Za-z0-9@._\-\s]{2,60}?)(?:\s+in|\s+on|\.|$)""",
        RegexOption.IGNORE_CASE,
    ),
)
