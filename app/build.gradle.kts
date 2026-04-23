// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

plugins {
    alias(libs.plugins.paisavault.android.application)
    alias(libs.plugins.paisavault.android.compose)
    alias(libs.plugins.paisavault.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.paisavault"

    defaultConfig {
        applicationId = "com.paisavault"
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        // Release signing is provided via ~/.gradle/gradle.properties + CI secrets.
        // See BUILD.md § 14 & § 29. Left unconfigured here so debug builds work out of the box.
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
            )
        }
    }
}

dependencies {
    // Feature modules wired into nav graph
    implementation(projects.feature.home)
    implementation(projects.feature.transactions)
    implementation(projects.feature.add)
    implementation(projects.feature.stats)
    implementation(projects.feature.smsReview)
    implementation(projects.feature.settings)
    implementation(projects.feature.onboarding)

    // Data layer wiring
    implementation(projects.data.transaction)
    implementation(projects.data.sms)
    implementation(projects.data.category)

    // Core
    implementation(projects.core.common)
    implementation(projects.core.domain)
    implementation(projects.core.designSystem)
    implementation(projects.core.ui)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.security)
    implementation(projects.core.ml)

    // Android core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.iconsext)
    implementation(libs.hilt.navigation.compose)

    // Serialization (for type-safe navigation)
    implementation(libs.kotlinx.serialization.json)

    // Logging
    implementation(libs.timber)

    // Performance
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)

    // Fragment activity (needed by BiometricPrompt)
    implementation("androidx.fragment:fragment-ktx:1.8.4")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
