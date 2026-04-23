// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.ingest

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.paisavault.data.sms.model.SmsMessage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SmsParseWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val ingestService: SmsIngestService,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.success()
        val body = inputData.getString(KEY_BODY) ?: return Result.success()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, System.currentTimeMillis())

        val sms = SmsMessage(
            id = 0L,
            sender = sender,
            body = body,
            receivedAtMillis = timestamp,
        )
        ingestService.ingest(sms)
        return Result.success()
    }

    companion object {
        const val KEY_SENDER = "sender"
        const val KEY_BODY = "body"
        const val KEY_TIMESTAMP = "timestamp"
    }
}
