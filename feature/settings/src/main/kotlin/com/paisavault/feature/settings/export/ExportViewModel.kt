// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisavault.core.common.result.Result
import com.paisavault.core.domain.usecase.ExportEncryptedCsvUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportEncryptedCsv: ExportEncryptedCsvUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ExportUiState())
    val state: StateFlow<ExportUiState> = _state.asStateFlow()

    fun startExport(targetUri: String, passphrase: CharArray) {
        _state.update { it.copy(isInProgress = true, error = null, success = false) }
        viewModelScope.launch {
            when (val result = exportEncryptedCsv(targetUri, passphrase)) {
                is Result.Success -> _state.update {
                    it.copy(isInProgress = false, success = true)
                }
                is Result.Failure -> _state.update {
                    it.copy(isInProgress = false, error = ExportError.Generic)
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.value = ExportUiState()
    }
}

data class ExportUiState(
    val isInProgress: Boolean = false,
    val success: Boolean = false,
    val error: ExportError? = null,
)

enum class ExportError { Generic, Mismatch, TooShort }
