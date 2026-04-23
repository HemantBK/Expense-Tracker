// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.error.DomainError
import com.paisavault.core.common.result.Result
import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.model.Money
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.model.TransactionSource
import com.paisavault.core.domain.model.TransactionType
import com.paisavault.core.domain.repository.TransactionRepository
import kotlinx.datetime.Instant
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(input: Input): Result<Long> {
        val validated = validate(input) ?: return Result.Failure(DomainError.Validation.AmountInvalid)
        return repository.add(validated)
    }

    private fun validate(input: Input): Transaction? {
        if (input.amountMinor <= 0) return null
        if (input.date > clock.now()) return null
        if (input.merchant.isBlank() || input.merchant.length > MAX_MERCHANT_LEN) return null
        val note = input.note?.takeIf { it.length <= MAX_NOTE_LEN }
        return Transaction(
            id = 0,
            amount = Money(minorUnits = input.amountMinor, currency = input.currency),
            type = input.type,
            merchant = input.merchant.trim(),
            categoryId = input.categoryId,
            date = input.date,
            note = note,
            source = TransactionSource.Manual,
            reference = null,
            rawSmsId = null,
            mlConfidence = null,
            userVerified = true,
        )
    }

    data class Input(
        val amountMinor: Long,
        val currency: String = "INR",
        val type: TransactionType = TransactionType.Debit,
        val merchant: String,
        val categoryId: Long,
        val date: Instant,
        val note: String? = null,
    )

    private companion object {
        const val MAX_MERCHANT_LEN = 200
        const val MAX_NOTE_LEN = 500
    }
}
