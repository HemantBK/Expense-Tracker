// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.model

/** Wire representation of an inbox SMS as surfaced by BroadcastReceiver / ContentResolver. */
data class SmsMessage(
    val id: Long,
    val sender: String,
    val body: String,
    val receivedAtMillis: Long,
)
