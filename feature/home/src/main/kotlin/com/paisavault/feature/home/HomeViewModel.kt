// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.home

import androidx.lifecycle.viewModelScope
import com.paisavault.core.domain.usecase.ObserveMonthlyDashboard
import com.paisavault.core.domain.usecase.ObservePendingSmsCount
import com.paisavault.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeMonthlyDashboard: ObserveMonthlyDashboard,
    observePendingSmsCount: ObservePendingSmsCount,
) : MviViewModel<HomeState, HomeEvent, HomeEffect>(HomeState()) {

    init {
        combine(observeMonthlyDashboard(), observePendingSmsCount()) { dashboard, pending ->
            dashboard to pending
        }
            .onEach { (dashboard, pending) ->
                setState {
                    copy(
                        isLoading = false,
                        monthTotalMinor = dashboard.totalDebitMinor,
                        recentTransactions = dashboard.recentTransactions,
                        breakdown = dashboard.categoryBreakdown,
                        pendingSmsCount = pending,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.OnAddClick -> sendEffect(HomeEffect.NavigateToAdd)
            HomeEvent.OnSeeAllClick -> sendEffect(HomeEffect.NavigateToTransactions)
            is HomeEvent.OnTransactionClick -> sendEffect(HomeEffect.NavigateToEdit(event.id))
            HomeEvent.OnReviewSmsClick -> sendEffect(HomeEffect.NavigateToSmsReview)
        }
    }
}
