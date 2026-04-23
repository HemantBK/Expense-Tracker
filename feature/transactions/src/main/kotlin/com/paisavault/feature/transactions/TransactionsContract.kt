// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.transactions

import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.ui.mvi.UiEffect
import com.paisavault.core.ui.mvi.UiEvent
import com.paisavault.core.ui.mvi.UiState

data class TransactionsState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val categoriesById: Map<Long, Category> = emptyMap(),
    val currency: String = "INR",
) : UiState

sealed interface TransactionsEvent : UiEvent {
    data class OnTxnClick(val id: Long) : TransactionsEvent
    data class OnDelete(val id: Long) : TransactionsEvent
}

sealed interface TransactionsEffect : UiEffect {
    data class NavigateToEdit(val id: Long) : TransactionsEffect
}
