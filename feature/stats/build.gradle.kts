// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.feature)
}

android {
    namespace = "com.paisavault.feature.stats"
}

dependencies {
    // Native Compose canvas chart for now. Vico will be added in 1.1 polish pass.
}
