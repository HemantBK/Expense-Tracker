// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.library)
    alias(libs.plugins.paisavault.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.paisavault.core.ml"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // TensorFlow Lite runtime is retained for future model upgrades. The current
    // merchant classifier is pure-Kotlin logistic regression — see ADR-0007.
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    // OCR (Tesseract 5 wrapper — Apache 2.0). Requires eng.traineddata in assets/tessdata/.
    implementation(libs.tesseract4android)
}
