// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.ingest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * Receives incoming-SMS broadcasts. Runs briefly on main thread — we MUST NOT do DB work
 * here. Instead we hand the parsed envelope (sender + body + timestamp) to WorkManager.
 */
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return

        // Reassemble multipart SMS by sender (common case: one sender in a broadcast).
        val bySender = messages.groupBy { it.originatingAddress.orEmpty() }
        for ((sender, parts) in bySender) {
            if (sender.isBlank()) continue
            val body = parts.joinToString(separator = "") { it.messageBody.orEmpty() }
            val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()
            enqueue(context, sender, body, timestamp)
        }
    }

    private fun enqueue(context: Context, sender: String, body: String, timestamp: Long) {
        val data = Data.Builder()
            .putString(SmsParseWorker.KEY_SENDER, sender)
            .putString(SmsParseWorker.KEY_BODY, body)
            .putLong(SmsParseWorker.KEY_TIMESTAMP, timestamp)
            .build()
        val request = OneTimeWorkRequestBuilder<SmsParseWorker>().setInputData(data).build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
