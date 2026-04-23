// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.model

import kotlinx.serialization.Serializable

/**
 * On-disk schema for a trained merchant classifier. Produced by
 * `ml-training/train.py --out core/ml/src/main/assets/model_v1.json`.
 *
 * Size of a typical model:
 *  - 8 categories x 1024 features = 8192 floats = ~32 KB intercepts + coefficients.
 */
@Serializable
internal data class ModelWeights(
    val modelId: String,
    val version: Int,
    val nFeatures: Int,
    val ngramMin: Int,
    val ngramMax: Int,
    val labels: List<String>,
    val intercepts: List<Float>,
    val coefficients: List<List<Float>>,
)
