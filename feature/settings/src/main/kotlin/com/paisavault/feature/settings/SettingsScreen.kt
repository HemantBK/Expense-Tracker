// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.theme.PaisaTokens
import com.paisavault.core.security.biometric.BiometricLockManager

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val allGranted = grants.values.all { it }
        viewModel.onEvent(SettingsEvent.OnSmsPermissionResult(allGranted))
        if (allGranted) viewModel.onEvent(SettingsEvent.OnStartSmsScan)
    }

    // Sync permission state on first composition.
    LaunchedEffect(Unit) {
        val granted = hasSmsPermissions(context)
        viewModel.onEvent(SettingsEvent.OnSmsPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SettingsEffect.RequestBiometricToEnableLock -> {
                    val activityRef = activity ?: return@collect
                    BiometricLockManager(activityRef).authenticate(
                        title = context.getString(R.string.settings_biometric_title),
                        subtitle = context.getString(R.string.settings_biometric_subtitle),
                        onSuccess = { viewModel.confirmEnableLock() },
                        onError = { _, _ -> /* keep toggle off */ },
                    )
                }
                SettingsEffect.RequestSmsPermissions -> {
                    smsPermissionLauncher.launch(
                        arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS),
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(PaisaTokens.Spacing.m),
            verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.m),
        ) {
            SettingsSwitchRow(
                title = stringResource(R.string.settings_lock_title),
                subtitle = stringResource(R.string.settings_lock_subtitle),
                checked = state.lockEnabled,
                onCheckedChange = { viewModel.onEvent(SettingsEvent.OnLockToggle(it)) },
            )

            SmsSection(
                state = state,
                onEnableClick = { viewModel.onEvent(SettingsEvent.OnStartSmsScan) },
            )

            SettingsNavRow(
                title = stringResource(R.string.settings_budgets_entry),
                subtitle = stringResource(R.string.settings_budgets_subtitle),
                onClick = onNavigateToBudgets,
            )

            SettingsNavRow(
                title = stringResource(R.string.settings_export_entry),
                subtitle = stringResource(R.string.settings_export_subtitle),
                onClick = onNavigateToExport,
            )

            Text(
                text = stringResource(R.string.settings_about),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = PaisaTokens.Spacing.l),
            )
            Text(
                text = stringResource(R.string.settings_about_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics { role = Role.Switch },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.padding(end = PaisaTokens.Spacing.m)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SmsSection(state: SettingsState, onEnableClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s),
        modifier = Modifier.fillMaxWidth().padding(top = PaisaTokens.Spacing.l),
    ) {
        Text(
            text = stringResource(R.string.settings_sms_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.settings_sms_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.scanning) {
            LinearProgressIndicator(
                progress = { state.scanFraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.settings_sms_scanning, state.scanProcessed, state.scanTotal),
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            Button(onClick = onEnableClick, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (state.smsPermissionGranted) {
                        stringResource(R.string.settings_sms_rescan)
                    } else {
                        stringResource(R.string.settings_sms_enable)
                    },
                )
            }
        }
    }
}

@Composable
private fun SettingsNavRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = PaisaTokens.Spacing.s),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.padding(end = PaisaTokens.Spacing.m)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun hasSmsPermissions(context: android.content.Context): Boolean =
    listOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS).all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
