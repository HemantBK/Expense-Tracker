// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.component.CategoryColorDot
import com.paisavault.core.designsystem.component.EmptyState
import com.paisavault.core.designsystem.component.LoadingState
import com.paisavault.core.designsystem.component.MoneyText
import com.paisavault.core.designsystem.theme.PaisaTokens
import com.paisavault.core.domain.model.Transaction

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllClick: () -> Unit,
    onReviewSmsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToAdd -> onAddClick()
                HomeEffect.NavigateToTransactions -> onSeeAllClick()
                is HomeEffect.NavigateToEdit -> onTransactionClick(effect.id)
                HomeEffect.NavigateToSmsReview -> onReviewSmsClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                colors = TopAppBarDefaults.topAppBarColors(),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onEvent(HomeEvent.OnAddClick) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_add)) },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.recentTransactions.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.home_empty_title),
                    subtitle = stringResource(R.string.home_empty_subtitle),
                    modifier = Modifier.padding(padding),
                )
            }
            else -> HomeContent(
                state = state,
                onSeeAllClick = { viewModel.onEvent(HomeEvent.OnSeeAllClick) },
                onTxnClick = { viewModel.onEvent(HomeEvent.OnTransactionClick(it)) },
                onReviewSmsClick = { viewModel.onEvent(HomeEvent.OnReviewSmsClick) },
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onSeeAllClick: () -> Unit,
    onTxnClick: (Long) -> Unit,
    onReviewSmsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = PaisaTokens.Spacing.m)) {
        if (state.pendingSmsCount > 0) {
            PendingSmsBanner(count = state.pendingSmsCount, onClick = onReviewSmsClick)
        }
        MonthTotalCard(state)
        TopCategoryRow(state)
        RecentHeader(onSeeAllClick)
        LazyColumn {
            items(state.recentTransactions, key = { it.id }) { txn ->
                TransactionRow(txn = txn, currency = state.currency, onClick = { onTxnClick(txn.id) })
            }
        }
    }
}

@Composable
private fun PendingSmsBanner(count: Int, onClick: () -> Unit) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = PaisaTokens.Spacing.s),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(PaisaTokens.Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = androidx.compose.ui.res.pluralStringResource(
                    R.plurals.home_pending_banner,
                    count,
                    count,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.home_pending_review),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MonthTotalCard(state: HomeState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaisaTokens.Spacing.m),
    ) {
        Text(
            text = stringResource(R.string.home_month_total_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MoneyText(
            amountMinor = state.monthTotalMinor,
            currencyCode = state.currency,
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

@Composable
private fun TopCategoryRow(state: HomeState) {
    if (state.breakdown.isEmpty()) return
    val top = state.breakdown.sortedByDescending { it.spend.totalMinor }.take(3)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = PaisaTokens.Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.m),
    ) {
        top.forEach { item ->
            Column(modifier = Modifier.padding(end = PaisaTokens.Spacing.s)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryColorDot(item.category?.colorArgb ?: 0xFF90A4AE.toInt())
                    Text(
                        text = item.category?.name ?: stringResource(R.string.uncategorized),
                        modifier = Modifier.padding(start = PaisaTokens.Spacing.s),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                MoneyText(
                    amountMinor = item.spend.totalMinor,
                    currencyCode = state.currency,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun RecentHeader(onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaisaTokens.Spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_recent),
            style = MaterialTheme.typography.titleMedium,
        )
        TextButton(onClick = onSeeAllClick) {
            Text(stringResource(R.string.home_see_all))
        }
    }
}

@Composable
private fun TransactionRow(txn: Transaction, currency: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = PaisaTokens.Spacing.s),
    ) {
        Column {
            Text(text = txn.merchant, style = MaterialTheme.typography.bodyLarge)
            MoneyText(
                amountMinor = txn.amount.minorUnits,
                currencyCode = currency,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    // onClick ignored here; add Modifier.clickable when we style rows properly in 1.1 polish
    @Suppress("UNUSED_EXPRESSION")
    onClick
}
