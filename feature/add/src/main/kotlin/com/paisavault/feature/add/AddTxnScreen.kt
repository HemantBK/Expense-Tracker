// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.theme.PaisaTokens

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AddTxnScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTxnViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) viewModel.onEvent(AddTxnEvent.OnReceiptCaptured(bitmap))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddTxnEffect.NavigateBack -> onNavigateBack()
                is AddTxnEffect.ShowError -> snackbarHostState.showSnackbar(effect.message.resolve(context))
                is AddTxnEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.resolve(context))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(AddTxnEvent.OnCancelClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(PaisaTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.m),
        ) {
            if (!state.receiptScanUnavailable) {
                OutlinedButton(
                    onClick = { takePictureLauncher.launch(null) },
                    enabled = !state.isScanningReceipt,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.padding(end = PaisaTokens.Spacing.s),
                    )
                    Text(
                        if (state.isScanningReceipt) {
                            stringResource(R.string.add_scan_receipt_working)
                        } else {
                            stringResource(R.string.add_scan_receipt)
                        },
                    )
                }
            }

            OutlinedTextField(
                value = state.amountInput,
                onValueChange = { viewModel.onEvent(AddTxnEvent.OnAmountChange(it)) },
                label = { Text(stringResource(R.string.add_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.merchantInput,
                onValueChange = { viewModel.onEvent(AddTxnEvent.OnMerchantChange(it)) },
                label = { Text(stringResource(R.string.add_merchant)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.noteInput,
                onValueChange = { viewModel.onEvent(AddTxnEvent.OnNoteChange(it)) },
                label = { Text(stringResource(R.string.add_note_optional)) },
                modifier = Modifier.fillMaxWidth(),
            )

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.add_category),
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                )
                val suggestedName = state.suggestedCategoryId?.let { id ->
                    state.categories.firstOrNull { it.id == id }?.name
                }
                if (suggestedName != null) {
                    Text(
                        text = stringResource(R.string.add_suggested, suggestedName),
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    )
                }
            }
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s),
            ) {
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.onEvent(AddTxnEvent.OnCategorySelect(cat.id)) },
                        label = { Text(cat.name) },
                        modifier = Modifier.semantics { role = Role.RadioButton },
                    )
                }
            }

            Button(
                onClick = { viewModel.onEvent(AddTxnEvent.OnSaveClick) },
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = PaisaTokens.Spacing.s))
                }
                Text(stringResource(R.string.add_save))
            }
        }
    }
}
