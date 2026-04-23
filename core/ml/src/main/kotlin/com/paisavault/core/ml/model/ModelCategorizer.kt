// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.model

import android.content.Context
import com.paisavault.core.common.dispatcher.AppDispatchers
import com.paisavault.core.ml.CategoryPrediction
import com.paisavault.core.ml.MerchantCategorizer
import com.paisavault.core.ml.MerchantPreprocessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trained-model categorizer. Loads `model_v1.json` from assets on first use. Returns
 * [CategoryPrediction.Uncategorized] if no model is bundled, which lets the composite
 * categorizer fall back to rules only.
 */
@Singleton
internal class ModelCategorizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
) : MerchantCategorizer {

    private val loadMutex = Mutex()
    @Volatile private var loaded: Loaded? = null
    @Volatile private var loadAttempted = false

    override suspend fun categorize(merchant: String): CategoryPrediction {
        val model = ensureLoaded() ?: return CategoryPrediction.Uncategorized
        return withContext(dispatchers.default) {
            val normalized = MerchantPreprocessor.normalize(merchant)
            if (normalized.isBlank()) return@withContext CategoryPrediction.Uncategorized
            val features = model.vectorizer.transform(normalized)
            if (features.isEmpty()) return@withContext CategoryPrediction.Uncategorized
            val pred = model.classifier.predict(features)
            CategoryPrediction(
                label = pred.label,
                confidence = pred.probability,
                source = CategoryPrediction.Source.Model,
            )
        }
    }

    private suspend fun ensureLoaded(): Loaded? {
        loaded?.let { return it }
        return loadMutex.withLock {
            loaded?.let { return@withLock it }
            if (loadAttempted) return@withLock null
            loadAttempted = true
            loadFromAssets().also { loaded = it }
        }
    }

    private suspend fun loadFromAssets(): Loaded? = withContext(dispatchers.io) {
        try {
            val bytes = context.assets.open(MODEL_FILENAME).use { it.readBytes() }
            val json = Json { ignoreUnknownKeys = true; isLenient = true }
            val weights = json.decodeFromString(
                ModelWeights.serializer(),
                bytes.decodeToString(),
            )
            val vectorizer = HashingVectorizer(
                nFeatures = weights.nFeatures,
                ngramRange = weights.ngramMin..weights.ngramMax,
            )
            val classifier = LogisticRegression(
                labels = weights.labels,
                intercepts = weights.intercepts.toFloatArray(),
                coefficients = Array(weights.coefficients.size) {
                    weights.coefficients[it].toFloatArray()
                },
            )
            Timber.i("Loaded merchant model ${weights.modelId} v${weights.version}")
            Loaded(vectorizer, classifier)
        } catch (_: java.io.FileNotFoundException) {
            Timber.i("No merchant model found in assets — falling back to rules only.")
            null
        } catch (t: Throwable) {
            Timber.w(t, "Failed to load merchant model; falling back to rules only.")
            null
        }
    }

    private data class Loaded(val vectorizer: HashingVectorizer, val classifier: LogisticRegression)

    private companion object {
        const val MODEL_FILENAME = "model_v1.json"
    }
}
