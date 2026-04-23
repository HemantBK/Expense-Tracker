// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.repository.CategoryRepository
import com.paisavault.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock as KxClock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import javax.inject.Inject

/**
 * Builds a `(merchant, category)` CSV of the user's verified transactions, suitable for
 * retraining the merchant classifier. Only user-verified rows are included — unverified
 * SMS transactions would teach the model its own guesses.
 */
class ExportTrainingDataUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(): String {
        val since: Instant = KxClock.System.now()
            .minus(DateTimePeriod(years = MAX_YEARS), TimeZone.UTC)
        val until = KxClock.System.now()

        val txns = transactionRepository.observeInRange(since, until).first()
        val categoriesById = categoryRepository.observeAll().first().associateBy { it.id }

        return buildString {
            appendLine("merchant,category")
            txns.asSequence()
                .filter(Transaction::userVerified)
                .filter { it.merchant.isNotBlank() }
                .forEach { txn ->
                    val category = categoriesById[txn.categoryId]?.name ?: return@forEach
                    append(escapeCsv(txn.merchant))
                    append(',')
                    append(escapeCsv(category))
                    append('\n')
                }
        }
    }

    private fun escapeCsv(raw: String): String {
        val needsQuote = raw.contains(',') || raw.contains('"') || raw.contains('\n')
        val escaped = raw.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }

    private companion object {
        const val MAX_YEARS = 5
    }
}
