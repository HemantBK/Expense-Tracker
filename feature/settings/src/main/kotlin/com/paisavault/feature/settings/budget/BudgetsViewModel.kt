// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paisavault.core.domain.usecase.DeleteBudgetUseCase
import com.paisavault.core.domain.usecase.ObserveBudgetStatusesUseCase
import com.paisavault.core.domain.usecase.ObserveCategoriesUseCase
import com.paisavault.core.domain.usecase.SetBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    observeCategories: ObserveCategoriesUseCase,
    observeBudgetStatuses: ObserveBudgetStatusesUseCase,
    private val setBudget: SetBudgetUseCase,
    private val deleteBudget: DeleteBudgetUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetsUiState())
    val state: StateFlow<BudgetsUiState> = _state.asStateFlow()

    init {
        combine(observeCategories(), observeBudgetStatuses()) { cats, statuses -> cats to statuses }
            .onEach { (cats, statuses) ->
                _state.update { it.copy(isLoading = false, categories = cats, statuses = statuses) }
            }
            .launchIn(viewModelScope)
    }

    fun onSetBudget(categoryId: Long, amountMajor: Double) {
        val minor = (amountMajor * MINOR_PER_MAJOR).toLong()
        if (minor <= 0) return
        viewModelScope.launch { setBudget(categoryId = categoryId, amountMinor = minor) }
    }

    fun onDelete(categoryId: Long) {
        viewModelScope.launch { deleteBudget(categoryId) }
    }

    private companion object {
        const val MINOR_PER_MAJOR = 100.0
    }
}
