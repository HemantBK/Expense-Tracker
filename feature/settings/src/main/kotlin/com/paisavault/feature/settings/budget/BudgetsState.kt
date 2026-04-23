// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings.budget

import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.usecase.BudgetStatus

data class BudgetsUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val statuses: List<BudgetStatus> = emptyList(),
)
