// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.library)
    alias(libs.plugins.paisavault.hilt)
    alias(libs.plugins.paisavault.room)
}

android {
    namespace = "com.paisavault.core.database"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.security)
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.kotlinx.datetime)
}
