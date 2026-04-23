// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Long,
    val amount: Money,
    val type: TransactionType,
    val merchant: String,
    val categoryId: Long,
    val date: Instant,
    val note: String?,
    val source: TransactionSource,
    val reference: String?,
    val rawSmsId: Long?,
    val mlConfidence: Float?,
    val userVerified: Boolean,
)

@Serializable
enum class TransactionType { Debit, Credit }

@Serializable
enum class TransactionSource { Manual, Sms, Ocr }
