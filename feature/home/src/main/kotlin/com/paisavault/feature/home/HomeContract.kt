// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.home

import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.usecase.CategorySpendWithCategory
import com.paisavault.core.ui.mvi.UiEffect
import com.paisavault.core.ui.mvi.UiEvent
import com.paisavault.core.ui.mvi.UiState

data class HomeState(
    val isLoading: Boolean = true,
    val monthTotalMinor: Long = 0,
    val currency: String = "INR",
    val recentTransactions: List<Transaction> = emptyList(),
    val breakdown: List<CategorySpendWithCategory> = emptyList(),
    val pendingSmsCount: Int = 0,
) : UiState

sealed interface HomeEvent : UiEvent {
    data object OnAddClick : HomeEvent
    data class OnTransactionClick(val id: Long) : HomeEvent
    data object OnSeeAllClick : HomeEvent
    data object OnReviewSmsClick : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data object NavigateToAdd : HomeEffect
    data class NavigateToEdit(val id: Long) : HomeEffect
    data object NavigateToTransactions : HomeEffect
    data object NavigateToSmsReview : HomeEffect
}
