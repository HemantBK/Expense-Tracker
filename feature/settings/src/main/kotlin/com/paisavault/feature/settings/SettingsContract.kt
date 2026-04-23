// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings

import com.paisavault.core.ui.mvi.UiEffect
import com.paisavault.core.ui.mvi.UiEvent
import com.paisavault.core.ui.mvi.UiState

data class SettingsState(
    val lockEnabled: Boolean = false,
    val smsPermissionGranted: Boolean = false,
    val scanning: Boolean = false,
    val scanProcessed: Int = 0,
    val scanTotal: Int = 0,
    val appVersion: String = "",
) : UiState {
    val scanFraction: Float = if (scanTotal == 0) 0f else scanProcessed.toFloat() / scanTotal.toFloat()
}

sealed interface SettingsEvent : UiEvent {
    data class OnLockToggle(val enabled: Boolean) : SettingsEvent
    data class OnSmsPermissionResult(val granted: Boolean) : SettingsEvent
    data object OnStartSmsScan : SettingsEvent
}

sealed interface SettingsEffect : UiEffect {
    data object RequestBiometricToEnableLock : SettingsEffect
    data object RequestSmsPermissions : SettingsEffect
}
