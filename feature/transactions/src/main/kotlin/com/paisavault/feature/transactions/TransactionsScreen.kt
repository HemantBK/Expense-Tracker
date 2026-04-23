// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.component.CategoryColorDot
import com.paisavault.core.designsystem.component.EmptyState
import com.paisavault.core.designsystem.component.LoadingState
import com.paisavault.core.designsystem.component.MoneyText
import com.paisavault.core.designsystem.theme.PaisaTokens
import com.paisavault.core.domain.model.Transaction
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onEditTransaction: (Long) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TransactionsEffect.NavigateToEdit -> onEditTransaction(effect.id)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.txns_title)) }) },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.transactions.isEmpty() -> EmptyState(
                title = stringResource(R.string.txns_empty_title),
                modifier = Modifier.padding(padding),
            )
            else -> {
                val grouped = state.transactions.groupBy { txn ->
                    txn.date.toLocalDateTime(TimeZone.currentSystemDefault()).date
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(PaisaTokens.Spacing.m),
                ) {
                    grouped.forEach { (date, txns) ->
                        item(key = "header-$date") {
                            Text(
                                text = date.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = PaisaTokens.Spacing.s),
                            )
                        }
                        items(txns, key = { it.id }) { txn ->
                            TransactionRow(
                                txn = txn,
                                categoryName = state.categoriesById[txn.categoryId]?.name
                                    ?: stringResource(R.string.uncategorized),
                                categoryColorArgb = state.categoriesById[txn.categoryId]?.colorArgb
                                    ?: 0xFF90A4AE.toInt(),
                                currency = state.currency,
                                onClick = { viewModel.onEvent(TransactionsEvent.OnTxnClick(txn.id)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    txn: Transaction,
    categoryName: String,
    categoryColorArgb: Int,
    currency: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PaisaTokens.Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CategoryColorDot(categoryColorArgb)
            Column(modifier = Modifier.padding(start = PaisaTokens.Spacing.m)) {
                Text(text = txn.merchant, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        MoneyText(
            amountMinor = txn.amount.minorUnits,
            currencyCode = currency,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
