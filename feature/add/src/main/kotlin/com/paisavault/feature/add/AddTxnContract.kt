// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.add

import android.graphics.Bitmap
import com.paisavault.core.domain.model.Category
import com.paisavault.core.ui.UiText
import com.paisavault.core.ui.mvi.UiEffect
import com.paisavault.core.ui.mvi.UiEvent
import com.paisavault.core.ui.mvi.UiState
import kotlinx.datetime.Instant

data class AddTxnState(
    val amountInput: String = "",
    val merchantInput: String = "",
    val noteInput: String = "",
    val selectedCategoryId: Long? = null,
    val userPickedCategory: Boolean = false,
    val suggestedCategoryId: Long? = null,
    val date: Instant? = null,
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isScanningReceipt: Boolean = false,
    val receiptScanUnavailable: Boolean = false,
    val validationError: UiText? = null,
) : UiState {
    val isSaveEnabled: Boolean
        get() = amountInput.toDoubleOrNull()?.let { it > 0 } == true &&
            merchantInput.isNotBlank() &&
            selectedCategoryId != null &&
            !isSaving
}

sealed interface AddTxnEvent : UiEvent {
    data class OnAmountChange(val value: String) : AddTxnEvent
    data class OnMerchantChange(val value: String) : AddTxnEvent
    data class OnNoteChange(val value: String) : AddTxnEvent
    data class OnCategorySelect(val id: Long) : AddTxnEvent
    data class OnDateChange(val instant: Instant) : AddTxnEvent
    data class OnReceiptCaptured(val bitmap: Bitmap) : AddTxnEvent
    data object OnSaveClick : AddTxnEvent
    data object OnCancelClick : AddTxnEvent
}

sealed interface AddTxnEffect : UiEffect {
    data object NavigateBack : AddTxnEffect
    data class ShowError(val message: UiText) : AddTxnEffect
    data class ShowMessage(val message: UiText) : AddTxnEffect
}
