// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.smsreview

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.component.EmptyState
import com.paisavault.core.designsystem.component.LoadingState
import com.paisavault.core.designsystem.component.MoneyText
import com.paisavault.core.designsystem.theme.PaisaTokens
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.model.Transaction

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SmsReviewScreen(viewModel: SmsReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.review_title)) }) },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.pending.isEmpty() -> EmptyState(
                title = stringResource(R.string.review_empty_title),
                subtitle = stringResource(R.string.review_empty_subtitle),
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(PaisaTokens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.m),
            ) {
                items(state.pending, key = { it.id }) { txn ->
                    PendingCard(
                        txn = txn,
                        categories = state.categories,
                        currency = state.currency,
                        onConfirm = { catId -> viewModel.onEvent(SmsReviewEvent.OnConfirm(txn.id, catId)) },
                        onReject = { viewModel.onEvent(SmsReviewEvent.OnReject(txn.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingCard(
    txn: Transaction,
    categories: List<Category>,
    currency: String,
    onConfirm: (Long?) -> Unit,
    onReject: () -> Unit,
) {
    var selectedCategory by remember(txn.id) { mutableStateOf(txn.categoryId) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = PaisaTokens.Elevation.card),
    ) {
        Column(
            modifier = Modifier.padding(PaisaTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.padding(end = PaisaTokens.Spacing.s)) {
                    Text(text = txn.merchant, style = MaterialTheme.typography.titleMedium)
                    txn.mlConfidence?.let { conf ->
                        Text(
                            text = stringResource(R.string.review_confidence, (conf * PERCENT).toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                MoneyText(
                    amountMinor = txn.amount.minorUnits,
                    currencyCode = currency,
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Text(
                text = stringResource(R.string.review_pick_category),
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s),
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.id,
                        onClick = { selectedCategory = cat.id },
                        label = { Text(cat.name) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s),
            ) {
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.review_reject))
                }
                Button(onClick = { onConfirm(selectedCategory) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.review_confirm))
                }
            }
        }
    }
}

private const val PERCENT = 100
