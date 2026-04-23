// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.library)
    alias(libs.plugins.paisavault.android.compose)
}

android {
    namespace = "com.paisavault.core.designsystem"
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.iconsext)
    implementation(libs.kotlinx.collections.immutable)
}
