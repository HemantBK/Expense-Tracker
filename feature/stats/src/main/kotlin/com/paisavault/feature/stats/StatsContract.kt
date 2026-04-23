// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.stats

import com.paisavault.core.domain.insight.MonthlyInsight
import com.paisavault.core.domain.usecase.BudgetStatus
import com.paisavault.core.domain.usecase.CategorySpendWithCategory
import com.paisavault.core.ui.mvi.UiEffect
import com.paisavault.core.ui.mvi.UiEvent
import com.paisavault.core.ui.mvi.UiState

data class StatsState(
    val isLoading: Boolean = true,
    val totalMinor: Long = 0,
    val currency: String = "INR",
    val breakdown: List<CategorySpendWithCategory> = emptyList(),
    val budgets: List<BudgetStatus> = emptyList(),
    val insights: List<MonthlyInsight> = emptyList(),
) : UiState

sealed interface StatsEvent : UiEvent
sealed interface StatsEffect : UiEffect
