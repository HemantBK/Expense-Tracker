// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.smsreview

import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.ui.mvi.UiEffect
import com.paisavault.core.ui.mvi.UiEvent
import com.paisavault.core.ui.mvi.UiState

data class SmsReviewState(
    val isLoading: Boolean = true,
    val pending: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val currency: String = "INR",
) : UiState

sealed interface SmsReviewEvent : UiEvent {
    data class OnConfirm(val id: Long, val categoryId: Long?) : SmsReviewEvent
    data class OnReject(val id: Long) : SmsReviewEvent
}

sealed interface SmsReviewEffect : UiEffect
