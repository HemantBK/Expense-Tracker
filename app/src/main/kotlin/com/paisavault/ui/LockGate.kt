// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.paisavault.core.security.biometric.BiometricLockManager
import com.paisavault.core.security.lock.LockState
import com.paisavault.navigation.PaisaNavHost

@Composable
fun LockGate(
    activity: FragmentActivity,
    viewModel: LockGateViewModel = hiltViewModel(),
) {
    val state by viewModel.lockState.collectAsState(initial = LockState.Unlocked)

    when (state) {
        LockState.Unlocked -> PaisaNavHost()
        LockState.Locked -> LockedScreen(
            onAuthenticate = {
                BiometricLockManager(activity).authenticate(
                    title = "Unlock PaisaVault",
                    subtitle = "Confirm with biometrics or device PIN",
                    onSuccess = { viewModel.unlock() },
                )
            },
        )
    }

    LaunchedEffect(state) {
        if (state == LockState.Locked) {
            BiometricLockManager(activity).authenticate(
                title = "Unlock PaisaVault",
                subtitle = "Confirm with biometrics or device PIN",
                onSuccess = { viewModel.unlock() },
            )
        }
    }
}

@Composable
private fun LockedScreen(onAuthenticate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "PaisaVault is locked",
            style = MaterialTheme.typography.titleLarge,
        )
        Button(onClick = onAuthenticate, modifier = Modifier.padding(top = 24.dp)) {
            Text("Unlock")
        }
    }
}
