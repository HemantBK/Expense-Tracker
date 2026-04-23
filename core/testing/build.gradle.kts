// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.library)
}

android {
    namespace = "com.paisavault.core.testing"
}

dependencies {
    api(projects.core.common)
    api(projects.core.domain)
    api(libs.junit)
    api(libs.turbine)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotest.assertions.core)
}
