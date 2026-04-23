// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Listens to the process-level [androidx.lifecycle.ProcessLifecycleOwner]. When the app
 * leaves foreground for more than [timeoutMillis], locks the app.
 */
@Singleton
class AutoLockObserver @Inject constructor(
    private val lockRepository: LockRepository,
) : DefaultLifecycleObserver {

    private var backgroundedAtMillis: Long = 0L
    private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMillis = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        val now = System.currentTimeMillis()
        if (backgroundedAtMillis > 0 && now - backgroundedAtMillis >= timeoutMillis) {
            lockRepository.lock()
        }
        backgroundedAtMillis = 0L
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 60_000L
    }
}
