// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.smsreview

import androidx.lifecycle.viewModelScope
import com.paisavault.core.domain.model.TransactionSource
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.core.domain.usecase.ConfirmSmsTransactionUseCase
import com.paisavault.core.domain.usecase.ObserveCategoriesUseCase
import com.paisavault.core.domain.usecase.ObservePendingSmsUseCase
import com.paisavault.core.domain.usecase.RejectSmsTransactionUseCase
import com.paisavault.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsReviewViewModel @Inject constructor(
    observePending: ObservePendingSmsUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val confirmUseCase: ConfirmSmsTransactionUseCase,
    private val rejectUseCase: RejectSmsTransactionUseCase,
    private val repository: TransactionRepository,
) : MviViewModel<SmsReviewState, SmsReviewEvent, SmsReviewEffect>(SmsReviewState()) {

    init {
        combine(observePending(), observeCategories()) { pending, cats -> pending to cats }
            .onEach { (pending, cats) ->
                setState {
                    copy(
                        isLoading = false,
                        pending = pending,
                        categories = cats,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SmsReviewEvent) {
        when (event) {
            is SmsReviewEvent.OnConfirm -> confirm(event.id, event.categoryId)
            is SmsReviewEvent.OnReject -> viewModelScope.launch { rejectUseCase(event.id) }
        }
    }

    private fun confirm(id: Long, categoryId: Long?) {
        viewModelScope.launch {
            val current = state.value.pending.firstOrNull { it.id == id } ?: return@launch
            if (categoryId != null && categoryId != current.categoryId) {
                repository.update(
                    current.copy(
                        categoryId = categoryId,
                        userVerified = true,
                        source = TransactionSource.Sms,
                    ),
                )
            } else {
                confirmUseCase(id)
            }
        }
    }
}
