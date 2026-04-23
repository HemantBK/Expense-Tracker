// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.ingest

import com.paisavault.core.common.result.Result
import com.paisavault.core.domain.classifier.ClassifiedCategory
import com.paisavault.core.domain.classifier.MerchantClassifier
import com.paisavault.core.domain.model.Money
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.model.TransactionSource
import com.paisavault.core.domain.repository.CategoryRepository
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.data.sms.model.SmsMessage
import com.paisavault.data.sms.parser.ParsedTxn
import com.paisavault.data.sms.parser.SmsParser
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates: raw SMS -> parser -> ML categorizer -> persisted unverified transaction.
 * De-duplicates via reference or synthetic (amount + date-minute + merchant) key.
 */
@Singleton
class SmsIngestService @Inject constructor(
    private val parser: SmsParser,
    private val classifier: MerchantClassifier,
    private val categoryRepository: CategoryRepository,
    private val repository: TransactionRepository,
) {

    suspend fun ingest(sms: SmsMessage) {
        val parsed = parser.parse(sms) ?: run {
            Timber.d("No parser matched SMS from ${sms.sender}")
            return
        }
        val classification = classifier.classify(parsed.merchant)
        val categoryId = resolveCategoryId(classification)
        val txn = parsed.toTransaction(
            rawSmsId = sms.id,
            categoryId = categoryId,
            mlConfidence = maxOf(parsed.confidence, classification.confidence),
        )
        when (val result = repository.add(txn)) {
            is Result.Success -> Timber.i(
                "Ingested SMS txn %s %d into category=%d (conf=%.2f)",
                parsed.bankName, parsed.amountMinor, categoryId, classification.confidence,
            )
            is Result.Failure -> Timber.w("Failed to ingest SMS txn: ${result.error}")
        }
    }

    private suspend fun resolveCategoryId(classification: ClassifiedCategory): Long {
        if (!classification.isUsable) return UNCATEGORIZED_ID
        val categories = categoryRepository.observeAll().first()
        val match = categories.firstOrNull { it.name.equals(classification.label, ignoreCase = true) }
        return match?.id ?: UNCATEGORIZED_ID
    }

    private fun ParsedTxn.toTransaction(
        rawSmsId: Long,
        categoryId: Long,
        mlConfidence: Float,
    ): Transaction = Transaction(
        id = 0,
        amount = Money(minorUnits = amountMinor, currency = currency),
        type = type,
        merchant = merchant,
        categoryId = categoryId,
        date = Instant.fromEpochMilliseconds(dateEpochMillis),
        note = null,
        source = TransactionSource.Sms,
        reference = reference ?: syntheticReference(),
        rawSmsId = rawSmsId,
        mlConfidence = mlConfidence,
        userVerified = false,
    )

    private fun ParsedTxn.syntheticReference(): String {
        val minute = dateEpochMillis / MILLIS_PER_MIN
        return "$bankName-$minute-${amountMinor}-${merchant.hashCode()}"
    }

    private companion object {
        const val UNCATEGORIZED_ID: Long = 0L
        const val MILLIS_PER_MIN = 60_000L
    }
}
