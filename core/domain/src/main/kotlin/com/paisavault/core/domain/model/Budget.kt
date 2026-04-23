// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: Long,
    val categoryId: Long,
    val amount: Money,
    val period: BudgetPeriod,
    val startDate: Instant,
)

@Serializable
enum class BudgetPeriod { Monthly, Weekly }
