// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.add

import androidx.lifecycle.viewModelScope
import com.paisavault.core.common.result.Result
import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.usecase.AddTransactionUseCase
import com.paisavault.core.domain.usecase.ObserveCategoriesUseCase
import com.paisavault.core.domain.usecase.SuggestCategoryUseCase
import com.paisavault.core.ml.ocr.ReceiptOcr
import com.paisavault.core.ml.ocr.ReceiptParser
import com.paisavault.core.ui.UiText
import com.paisavault.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTxnViewModel @Inject constructor(
    private val addTransaction: AddTransactionUseCase,
    observeCategories: ObserveCategoriesUseCase,
    private val suggestCategory: SuggestCategoryUseCase,
    private val receiptOcr: ReceiptOcr,
    private val receiptParser: ReceiptParser,
    private val clock: Clock,
) : MviViewModel<AddTxnState, AddTxnEvent, AddTxnEffect>(AddTxnState()) {

    private var suggestJob: Job? = null

    init {
        setState { copy(date = clock.now()) }
        observeCategories()
            .onEach { cats ->
                setState {
                    copy(
                        categories = cats,
                        selectedCategoryId = selectedCategoryId ?: cats.firstOrNull()?.id,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: AddTxnEvent) {
        when (event) {
            is AddTxnEvent.OnAmountChange -> setState { copy(amountInput = event.value, validationError = null) }
            is AddTxnEvent.OnMerchantChange -> {
                setState { copy(merchantInput = event.value, validationError = null) }
                scheduleSuggest(event.value)
            }
            is AddTxnEvent.OnNoteChange -> setState { copy(noteInput = event.value) }
            is AddTxnEvent.OnCategorySelect -> {
                setState { copy(selectedCategoryId = event.id, userPickedCategory = true) }
            }
            is AddTxnEvent.OnDateChange -> setState { copy(date = event.instant) }
            is AddTxnEvent.OnReceiptCaptured -> scanReceipt(event.bitmap)
            AddTxnEvent.OnCancelClick -> sendEffect(AddTxnEffect.NavigateBack)
            AddTxnEvent.OnSaveClick -> save()
        }
    }

    private fun scanReceipt(bitmap: android.graphics.Bitmap) {
        setState { copy(isScanningReceipt = true) }
        viewModelScope.launch {
            val text = receiptOcr.recognize(bitmap)
            if (text.isNullOrBlank()) {
                setState { copy(isScanningReceipt = false, receiptScanUnavailable = true) }
                sendEffect(AddTxnEffect.ShowError(UiText.StringResource(R.string.add_ocr_unavailable)))
                return@launch
            }
            val parsed = receiptParser.parse(text)
            if (parsed == null) {
                setState { copy(isScanningReceipt = false) }
                sendEffect(AddTxnEffect.ShowError(UiText.StringResource(R.string.add_ocr_nothing_found)))
                return@launch
            }
            setState {
                copy(
                    isScanningReceipt = false,
                    amountInput = (parsed.amountMinor / MINOR_PER_MAJOR).toString(),
                    merchantInput = parsed.merchant ?: merchantInput,
                )
            }
            if (parsed.merchant != null) {
                scheduleSuggest(parsed.merchant)
            }
            sendEffect(AddTxnEffect.ShowMessage(UiText.StringResource(R.string.add_ocr_done)))
        }
    }

    private fun scheduleSuggest(raw: String) {
        suggestJob?.cancel()
        if (raw.length < SUGGEST_MIN_CHARS) {
            setState { copy(suggestedCategoryId = null) }
            return
        }
        suggestJob = viewModelScope.launch {
            delay(SUGGEST_DEBOUNCE_MS)
            val suggestion = suggestCategory(raw.trim())
            setState {
                val newSelected = if (!userPickedCategory && suggestion != null) {
                    suggestion.category.id
                } else {
                    selectedCategoryId
                }
                copy(
                    suggestedCategoryId = suggestion?.category?.id,
                    selectedCategoryId = newSelected,
                )
            }
        }
    }

    private fun save() {
        val s = state.value
        val amount = s.amountInput.toDoubleOrNull() ?: return
        val catId = s.selectedCategoryId ?: return
        val date = s.date ?: clock.now()
        val amountMinor = (amount * MINOR_PER_MAJOR).toLong()

        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = addTransaction(
                AddTransactionUseCase.Input(
                    amountMinor = amountMinor,
                    merchant = s.merchantInput,
                    categoryId = catId,
                    date = date,
                    note = s.noteInput.ifBlank { null },
                ),
            )
            setState { copy(isSaving = false) }
            when (result) {
                is Result.Success -> sendEffect(AddTxnEffect.NavigateBack)
                is Result.Failure -> sendEffect(AddTxnEffect.ShowError(UiText.StringResource(R.string.add_error_save)))
            }
        }
    }

    private companion object {
        const val MINOR_PER_MAJOR = 100.0
        const val SUGGEST_MIN_CHARS = 3
        const val SUGGEST_DEBOUNCE_MS = 300L
    }
}
