// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security.lock

import com.paisavault.core.security.KeystoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for whether the app content is currently visible (Unlocked) or
 * gated behind biometric auth (Locked). See BUILD.md § 23.2.
 */
@Singleton
class LockRepository @Inject constructor(
    private val keystoreManager: KeystoreManager,
) {
    private val _state = MutableStateFlow(initialState())
    val state: Flow<LockState> = _state.asStateFlow()

    fun isLockEnabled(): Boolean = keystoreManager.getBoolean(KEY_LOCK_ENABLED, default = false)

    fun setLockEnabled(enabled: Boolean) {
        keystoreManager.putBoolean(KEY_LOCK_ENABLED, enabled)
        _state.value = if (enabled) LockState.Locked else LockState.Unlocked
    }

    fun lock() {
        if (isLockEnabled()) {
            _state.value = LockState.Locked
        }
    }

    fun unlock() {
        _state.value = LockState.Unlocked
    }

    private fun initialState(): LockState =
        if (keystoreManager.getBoolean(KEY_LOCK_ENABLED, false)) LockState.Locked else LockState.Unlocked

    private companion object {
        const val KEY_LOCK_ENABLED = "lock_enabled"
    }
}
