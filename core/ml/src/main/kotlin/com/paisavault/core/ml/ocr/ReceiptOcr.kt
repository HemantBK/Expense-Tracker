// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.ocr

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import com.paisavault.core.common.dispatcher.AppDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-device OCR using Tesseract 5. Returns extracted text from a camera bitmap.
 *
 * The engine's tessdata directory is populated on first use by copying the bundled
 * `eng.traineddata` asset (expected at `core/ml/src/main/assets/tessdata/eng.traineddata`).
 * If the file is absent, OCR returns null and the UI treats it as unavailable.
 *
 * See BUILD.md § 13.2.
 */
@Singleton
class ReceiptOcr @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
) {

    private val initMutex = Mutex()
    @Volatile private var tess: TessBaseAPI? = null
    @Volatile private var initFailed = false

    suspend fun recognize(bitmap: Bitmap): String? {
        val engine = ensureInit() ?: return null
        return withContext(dispatchers.default) {
            try {
                engine.setImage(bitmap)
                engine.utF8Text?.trim()
            } catch (t: Throwable) {
                Timber.w(t, "OCR failed")
                null
            }
        }
    }

    private suspend fun ensureInit(): TessBaseAPI? {
        tess?.let { return it }
        if (initFailed) return null
        return initMutex.withLock {
            tess?.let { return@withLock it }
            if (initFailed) return@withLock null
            tess = initInternal()
            if (tess == null) initFailed = true
            tess
        }
    }

    private suspend fun initInternal(): TessBaseAPI? = withContext(dispatchers.io) {
        val tessDir = File(context.filesDir, TESS_DATA_DIR_NAME)
        val engFile = File(tessDir, ENG_TRAINEDDATA_NAME)
        if (!engFile.exists()) {
            val copied = runCatching { copyAssetToFile() }.getOrElse {
                Timber.w(it, "Could not copy eng.traineddata — OCR disabled")
                null
            } ?: return@withContext null
            if (!copied.exists()) {
                Timber.i("Tesseract traineddata not bundled; OCR disabled.")
                return@withContext null
            }
        }
        val api = TessBaseAPI()
        val initialized = api.init(context.filesDir.absolutePath, "eng")
        if (!initialized) {
            Timber.w("TessBaseAPI init failed")
            api.recycle()
            null
        } else {
            api
        }
    }

    private fun copyAssetToFile(): File {
        val assetPath = "$TESS_DATA_DIR_NAME/$ENG_TRAINEDDATA_NAME"
        val tessDir = File(context.filesDir, TESS_DATA_DIR_NAME)
        if (!tessDir.exists()) tessDir.mkdirs()
        val target = File(tessDir, ENG_TRAINEDDATA_NAME)
        context.assets.open(assetPath).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }

    private companion object {
        const val TESS_DATA_DIR_NAME = "tessdata"
        const val ENG_TRAINEDDATA_NAME = "eng.traineddata"
    }
}
