// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.transactions

import androidx.lifecycle.viewModelScope
import com.paisavault.core.domain.usecase.DeleteTransactionUseCase
import com.paisavault.core.domain.usecase.ObserveCategoriesUseCase
import com.paisavault.core.domain.usecase.ObserveMonthlyTransactions
import com.paisavault.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    observeMonthlyTransactions: ObserveMonthlyTransactions,
    observeCategories: ObserveCategoriesUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
) : MviViewModel<TransactionsState, TransactionsEvent, TransactionsEffect>(TransactionsState()) {

    init {
        combine(observeMonthlyTransactions(), observeCategories()) { txns, cats ->
            txns to cats
        }
            .onEach { (txns, cats) ->
                setState {
                    copy(
                        isLoading = false,
                        transactions = txns,
                        categoriesById = cats.associateBy { it.id },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: TransactionsEvent) {
        when (event) {
            is TransactionsEvent.OnTxnClick -> sendEffect(TransactionsEffect.NavigateToEdit(event.id))
            is TransactionsEvent.OnDelete -> viewModelScope.launch { deleteTransaction(event.id) }
        }
    }
}
