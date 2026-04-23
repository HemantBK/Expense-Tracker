// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.component.CategoryColorDot
import com.paisavault.core.designsystem.component.LoadingState
import com.paisavault.core.designsystem.component.MoneyText
import com.paisavault.core.designsystem.theme.PaisaTokens
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.usecase.BudgetStatus
import com.paisavault.feature.settings.R

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    onBack: () -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editTarget: Category? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.budgets_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        val budgetByCategoryId = state.statuses.associateBy { it.budget.categoryId }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(PaisaTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s),
        ) {
            item {
                Text(
                    text = stringResource(R.string.budgets_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.categories, key = { it.id }) { category ->
                CategoryBudgetRow(
                    category = category,
                    status = budgetByCategoryId[category.id],
                    onTap = { editTarget = category },
                    onDelete = { viewModel.onDelete(category.id) },
                )
            }
        }
    }

    val target = editTarget
    if (target != null) {
        SetBudgetDialog(
            category = target,
            currentMinor = state.statuses.firstOrNull { it.budget.categoryId == target.id }
                ?.budget?.amount?.minorUnits,
            onConfirm = { amount ->
                viewModel.onSetBudget(target.id, amount)
                editTarget = null
            },
            onDismiss = { editTarget = null },
        )
    }
}

@Composable
private fun CategoryBudgetRow(
    category: Category,
    status: BudgetStatus?,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTap)
            .padding(vertical = PaisaTokens.Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryColorDot(category.colorArgb)
        Column(
            modifier = Modifier.padding(horizontal = PaisaTokens.Spacing.m).weight(1f),
        ) {
            Text(category.name, style = MaterialTheme.typography.bodyLarge)
            if (status != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoneyText(status.spentMinor, "INR", style = MaterialTheme.typography.bodySmall)
                    Text(" / ", style = MaterialTheme.typography.bodySmall)
                    MoneyText(
                        status.budget.amount.minorUnits,
                        "INR",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text(
                    stringResource(R.string.budgets_not_set),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (status != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.budgets_delete))
            }
        }
    }
}

@Composable
private fun SetBudgetDialog(
    category: Category,
    currentMinor: Long?,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(category.id) {
        mutableStateOf(currentMinor?.let { (it / MAJOR_PER_MINOR).toString() } ?: "")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.budgets_dialog_title, category.name)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.budgets_amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = text.toDoubleOrNull()
                    if (amount != null && amount > 0) onConfirm(amount)
                },
            ) { Text(stringResource(R.string.budgets_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.budgets_cancel)) }
        },
    )
}

private const val MAJOR_PER_MINOR = 100.0
