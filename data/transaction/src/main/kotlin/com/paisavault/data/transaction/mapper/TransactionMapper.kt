// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.transaction.mapper

import com.paisavault.core.database.entity.TransactionEntity
import com.paisavault.core.domain.model.Money
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.model.TransactionSource
import com.paisavault.core.domain.model.TransactionType
import kotlinx.datetime.Instant

internal fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        amount = Money(minorUnits = amountMinor, currency = currency),
        type = type.toTxnType(),
        merchant = merchant,
        categoryId = categoryId ?: UNCATEGORIZED_ID,
        date = Instant.fromEpochMilliseconds(dateEpochMillis),
        note = note,
        source = source.toTxnSource(),
        reference = reference,
        rawSmsId = rawSmsId,
        mlConfidence = mlConfidence,
        userVerified = userVerified,
    )

internal fun Transaction.toEntity(createdAtMillis: Long): TransactionEntity =
    TransactionEntity(
        id = id,
        amountMinor = amount.minorUnits,
        currency = amount.currency,
        type = type.toColumn(),
        merchant = merchant,
        categoryId = categoryId.takeIf { it != UNCATEGORIZED_ID },
        dateEpochMillis = date.toEpochMilliseconds(),
        note = note,
        source = source.toColumn(),
        reference = reference,
        rawSmsId = rawSmsId,
        mlConfidence = mlConfidence,
        userVerified = userVerified,
        createdAtEpochMillis = createdAtMillis,
    )

private fun String.toTxnType(): TransactionType = when (this) {
    "DEBIT" -> TransactionType.Debit
    "CREDIT" -> TransactionType.Credit
    else -> error("Unknown transaction type '$this'")
}

private fun TransactionType.toColumn(): String = when (this) {
    TransactionType.Debit -> "DEBIT"
    TransactionType.Credit -> "CREDIT"
}

private fun String.toTxnSource(): TransactionSource = when (this) {
    "MANUAL" -> TransactionSource.Manual
    "SMS" -> TransactionSource.Sms
    "OCR" -> TransactionSource.Ocr
    else -> error("Unknown transaction source '$this'")
}

private fun TransactionSource.toColumn(): String = when (this) {
    TransactionSource.Manual -> "MANUAL"
    TransactionSource.Sms -> "SMS"
    TransactionSource.Ocr -> "OCR"
}

internal const val UNCATEGORIZED_ID: Long = 0L
