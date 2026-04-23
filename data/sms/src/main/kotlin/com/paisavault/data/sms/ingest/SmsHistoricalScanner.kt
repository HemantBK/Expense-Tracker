// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.ingest

import android.content.Context
import android.provider.Telephony
import com.paisavault.data.sms.model.SmsMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Reads SMS inbox via ContentResolver. Run once after permission grant.
 * Emits progress (count processed) so UI can show a progress bar.
 */
class SmsHistoricalScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ingestService: SmsIngestService,
) {

    /**
     * Returns a [Flow] of (processed, total) pairs as scanning progresses.
     * Completes when done. Does NOT throw on permission issues — returns 0/0.
     */
    fun scan(sinceEpochMillis: Long = 0L): Flow<Progress> = flow {
        val resolver = context.contentResolver
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
        )
        val selection = "${Telephony.Sms.DATE} >= ?"
        val selectionArgs = arrayOf(sinceEpochMillis.toString())
        val cursor = runCatching {
            resolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${Telephony.Sms.DATE} DESC",
            )
        }.getOrNull() ?: run {
            emit(Progress(0, 0))
            return@flow
        }
        cursor.use { c ->
            val total = c.count
            var processed = 0
            val idIdx = c.getColumnIndexOrThrow(Telephony.Sms._ID)
            val addrIdx = c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val bodyIdx = c.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val dateIdx = c.getColumnIndexOrThrow(Telephony.Sms.DATE)
            emit(Progress(processed, total))
            while (c.moveToNext()) {
                val sms = SmsMessage(
                    id = c.getLong(idIdx),
                    sender = c.getString(addrIdx) ?: "",
                    body = c.getString(bodyIdx) ?: "",
                    receivedAtMillis = c.getLong(dateIdx),
                )
                ingestService.ingest(sms)
                processed++
                if (processed % PROGRESS_EMIT_EVERY == 0 || processed == total) {
                    emit(Progress(processed, total))
                }
            }
            emit(Progress(processed, total))
        }
    }

    data class Progress(val processed: Int, val total: Int) {
        val fraction: Float = if (total == 0) 1f else processed.toFloat() / total.toFloat()
    }

    private companion object {
        const val PROGRESS_EMIT_EVERY = 10
    }
}
