// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser

import com.paisavault.core.domain.model.TransactionType

data class ParsedTxn(
    val amountMinor: Long,
    val currency: String = "INR",
    val type: TransactionType,
    val merchant: String,
    val dateEpochMillis: Long,
    val reference: String?,
    val bankName: String,
    val confidence: Float,
)
