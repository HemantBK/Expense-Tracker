// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.stats

import androidx.lifecycle.viewModelScope
import com.paisavault.core.domain.usecase.ObserveBudgetStatusesUseCase
import com.paisavault.core.domain.usecase.ObserveMonthlyDashboard
import com.paisavault.core.domain.usecase.ObserveMonthlyInsightsUseCase
import com.paisavault.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    observeMonthlyDashboard: ObserveMonthlyDashboard,
    observeBudgetStatuses: ObserveBudgetStatusesUseCase,
    observeMonthlyInsights: ObserveMonthlyInsightsUseCase,
) : MviViewModel<StatsState, StatsEvent, StatsEffect>(StatsState()) {

    init {
        combine(
            observeMonthlyDashboard(),
            observeBudgetStatuses(),
            observeMonthlyInsights(),
        ) { dashboard, budgets, insights ->
            Triple(dashboard, budgets, insights)
        }
            .onEach { (dashboard, budgets, insights) ->
                setState {
                    copy(
                        isLoading = false,
                        totalMinor = dashboard.totalDebitMinor,
                        breakdown = dashboard.categoryBreakdown.sortedByDescending { it.spend.totalMinor },
                        budgets = budgets,
                        insights = insights,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: StatsEvent) = Unit
}
