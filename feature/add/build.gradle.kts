// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.feature)
}

android {
    namespace = "com.paisavault.feature.add"
}

dependencies {
    implementation(projects.core.ml)
}
