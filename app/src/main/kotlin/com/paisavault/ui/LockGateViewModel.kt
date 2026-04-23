// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.ui

import androidx.lifecycle.ViewModel
import com.paisavault.core.security.lock.LockRepository
import com.paisavault.core.security.lock.LockState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LockGateViewModel @Inject constructor(
    private val lockRepository: LockRepository,
) : ViewModel() {
    val lockState: Flow<LockState> = lockRepository.state

    fun unlock() = lockRepository.unlock()
}
