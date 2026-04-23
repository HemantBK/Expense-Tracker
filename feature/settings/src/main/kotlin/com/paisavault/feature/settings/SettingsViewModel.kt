// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.settings

import androidx.lifecycle.viewModelScope
import com.paisavault.core.domain.usecase.ScanSmsHistoryUseCase
import com.paisavault.core.security.lock.LockRepository
import com.paisavault.core.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val lockRepository: LockRepository,
    private val scanSmsHistory: ScanSmsHistoryUseCase,
) : MviViewModel<SettingsState, SettingsEvent, SettingsEffect>(
    SettingsState(lockEnabled = lockRepository.isLockEnabled()),
) {

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnLockToggle -> handleLockToggle(event.enabled)
            is SettingsEvent.OnSmsPermissionResult -> setState { copy(smsPermissionGranted = event.granted) }
            SettingsEvent.OnStartSmsScan -> startScan()
        }
    }

    private fun handleLockToggle(enabled: Boolean) {
        if (enabled) {
            sendEffect(SettingsEffect.RequestBiometricToEnableLock)
        } else {
            lockRepository.setLockEnabled(false)
            setState { copy(lockEnabled = false) }
        }
    }

    fun confirmEnableLock() {
        lockRepository.setLockEnabled(true)
        setState { copy(lockEnabled = true) }
    }

    private fun startScan() {
        if (!state.value.smsPermissionGranted) {
            sendEffect(SettingsEffect.RequestSmsPermissions)
            return
        }
        setState { copy(scanning = true, scanProcessed = 0, scanTotal = 0) }
        scanSmsHistory()
            .onEach { progress ->
                setState { copy(scanProcessed = progress.processed, scanTotal = progress.total) }
            }
            .onCompletion { setState { copy(scanning = false) } }
            .launchIn(viewModelScope)
    }
}
