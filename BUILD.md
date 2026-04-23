# PaisaVault — Build Plan (v2, MAANG-grade)

> **This is the production-grade plan.** It bakes in multi-module architecture, convention plugins, CI/CD, quality gates, accessibility, internationalization, performance budgets, observability, and release engineering from day one. It is heavier than a hobby-project plan by design — the goal is a codebase that would pass design review at a top-tier company and be genuinely portfolio-worthy.

---

## Table of Contents

1. [Engineering Principles](#1-engineering-principles)
2. [Prerequisites & Setup](#2-prerequisites--setup)
3. [Roadmap Overview](#3-roadmap-overview)
4. [Multi-Module Architecture](#4-multi-module-architecture)
5. [Build Infrastructure](#5-build-infrastructure)
6. [Dependencies (Version Catalog)](#6-dependencies-version-catalog)
7. [Quality Gates](#7-quality-gates)
8. [Repository Hygiene](#8-repository-hygiene)
9. [Phase 0 — Foundations](#9-phase-0--foundations-1-2-weeks)
10. [Phase 1 — MVP](#10-phase-1--mvp-4-6-weeks)
11. [Phase 2 — SMS Automation](#11-phase-2--sms-automation-2-3-weeks)
12. [Phase 3 — AI/ML Categorization](#12-phase-3--aiml-categorization-3-4-weeks)
13. [Phase 4 — Insights & Polish](#13-phase-4--insights--polish-2-3-weeks)
14. [State Management (UDF/MVI)](#14-state-management-udfmvi)
15. [Error Taxonomy](#15-error-taxonomy)
16. [Navigation](#16-navigation)
17. [Accessibility](#17-accessibility)
18. [Internationalization](#18-internationalization)
19. [Theming & Design System](#19-theming--design-system)
20. [Performance Engineering](#20-performance-engineering)
21. [Observability (local-only)](#21-observability-local-only)
22. [Feature Flags](#22-feature-flags)
23. [Security Implementation](#23-security-implementation)
24. [Database & Migrations](#24-database--migrations)
25. [SMS Parsing Strategy](#25-sms-parsing-strategy)
26. [ML Pipeline](#26-ml-pipeline)
27. [Testing Strategy](#27-testing-strategy)
28. [CI/CD Pipeline](#28-cicd-pipeline)
29. [Release Engineering](#29-release-engineering)
30. [Disaster Recovery](#30-disaster-recovery)
31. [Documentation Practice](#31-documentation-practice)
32. [iOS Planning & KMP Evaluation](#32-ios-planning--kmp-evaluation)
33. [Licensing & FOSS Compliance](#33-licensing--foss-compliance)
34. [Development Cadence](#34-development-cadence)

---

## 1. Engineering Principles

Non-negotiable principles. Every choice in this plan can be traced to one of these.

1. **Local-first, always.** The user's data never leaves their device.
2. **FOSS only.** Every runtime dependency is OSI-approved. Enforced by CI.
3. **Security by default.** Encrypted at rest, biometric-gated, minimum permissions. No opt-out of safety.
4. **Accessibility is not optional.** WCAG AA from Phase 1, not bolted on.
5. **Measured over aspirational.** Every performance claim is a number in CI.
6. **Deletable over addable.** Prefer removing code to adding abstractions.
7. **Test as spec.** If it matters, there's a test. Tests read as documentation.
8. **Automate quality.** Humans review design; machines enforce style.
9. **Unidirectional state.** UI reads state, sends events, observes effects. No two-way binding.
10. **Explicit over implicit.** Type-safe navigation, sealed domain errors, explicit nullability.
11. **One source of truth.** Version catalog for deps, convention plugins for build config, single DB for state.
12. **Ship small, ship often.** Each phase ends in a release.

---

## 2. Prerequisites & Setup

### Install
- [ ] **JDK 17** — Eclipse Temurin (https://adoptium.net) or Microsoft OpenJDK (`winget install Microsoft.OpenJDK.17`). Both OpenJDK-based, GPL + Classpath exception.
- [ ] **Android Studio Ladybug** (2024.2.1) or newer. Free; core is Apache 2.0. Strict-FOSS alternative: **IntelliJ IDEA Community** + Android plugin.
- [ ] **Git** ≥ 2.40 — `winget install Git.Git`.
- [ ] **Node.js** ≥ 20 (for commit hooks) — `winget install OpenJS.NodeJS.LTS`.
- [ ] **GitHub CLI** (for release automation) — `winget install GitHub.cli`.
- [ ] **Android SDK** — Platform 35, minSdk 28, Build-Tools 35.0.0.
- [ ] **Physical test device** with USB debugging enabled.

### Verify
```bash
java -version              # 17.x
git --version              # 2.40+
node --version             # 20+
gh --version               # 2.x
adb devices                # your phone listed
./gradlew --version        # after clone
```

### IDE configuration
- [ ] Import `.editorconfig` settings
- [ ] Install plugins: Kotlin, Compose, Ktlint, Detekt, ADB Idea
- [ ] Enable "Reformat code on save" + "Optimize imports on save"
- [ ] Configure "Kotlin Compiler Options → `-Xcontext-receivers`" if used

### Learning prerequisites (if new to Android)
| Topic | Resource | Time |
|---|---|---|
| Kotlin fundamentals | https://kotlinlang.org/docs/kotlin-tour-welcome.html | 1 day |
| Coroutines & Flow | https://kotlinlang.org/docs/coroutines-guide.html | 1 day |
| Jetpack Compose | https://developer.android.com/courses/pathways/compose | 2 days |
| Room + Flow | https://developer.android.com/training/data-storage/room | 4 hrs |
| Hilt | https://developer.android.com/training/dependency-injection/hilt-android | 3 hrs |
| Android architecture guide | https://developer.android.com/topic/architecture | 4 hrs |

---

## 3. Roadmap Overview

```
┌────────────────┐   ┌─────────────┐   ┌─────────────┐   ┌──────────────┐   ┌─────────────┐
│   Phase 0      │──▶│   Phase 1   │──▶│   Phase 2   │──▶│   Phase 3    │──▶│   Phase 4   │
│  Foundations   │   │     MVP     │   │ SMS parsing │   │  ML catego-  │   │  Insights,  │
│ (infra, CI,    │   │ (manual     │   │  + review   │   │   rization   │   │ OCR, export │
│  quality gates)│   │  entry)     │   │   queue     │   │              │   │             │
└────────────────┘   └─────────────┘   └─────────────┘   └──────────────┘   └─────────────┘
   1–2 weeks           4–6 weeks         2–3 weeks         3–4 weeks           2–3 weeks
```

Each phase ends with a tagged release (v0.1.0, v0.2.0, v0.3.0, v0.4.0, v1.0.0).

---

## 4. Multi-Module Architecture

### 4.1 Why multi-module

- **Parallel compilation** — independent modules compile in parallel, incremental builds stay fast as codebase grows
- **Enforced boundaries** — the Gradle module graph IS the architecture diagram; wrong dependencies won't compile
- **Team scalability** — even solo, you enforce discipline; later, contributors can work on one feature without knowing the whole tree
- **Testability** — fake implementations per module boundary
- **Compose performance** — smaller modules = better stability inference

### 4.2 Module graph

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                    :app                                     │
│                    (entry, DI wiring, navigation root)                      │
└───────────┬───────────────────┬──────────────────┬─────────────────────────┘
            │                   │                  │
   ┌────────▼──────────┐ ┌─────▼─────────┐ ┌──────▼──────┐   ┌─────────────┐
   │  :feature:home    │ │ :feature:...  │ │ :feature:   │ … │  :data:*    │
   │                   │ │               │ │  settings   │   │             │
   └────┬──────────────┘ └─────┬─────────┘ └──────┬──────┘   └─────┬───────┘
        │                      │                  │                 │
        └──────────────────────┼──────────────────┘                 │
                               │                                    │
                  ┌────────────▼─────────────┐                      │
                  │   :core:domain           │◀─────────────────────┘
                  │  (pure Kotlin, no Android)│
                  └────────────┬─────────────┘
                               │
           ┌───────────────────┼────────────────────┐
           │                   │                    │
   ┌───────▼────────┐  ┌───────▼──────────┐  ┌─────▼──────┐
   │ :core:common   │  │ :core:design-    │  │ :core:ui   │
   │ (Result, utils)│  │   system (M3)    │  │ (shared UI)│
   └────────────────┘  └──────────────────┘  └────────────┘

   ┌────────────────┐  ┌──────────────────┐  ┌────────────┐  ┌──────────────┐
   │ :core:database │  │ :core:datastore  │  │ :core:     │  │  :core:ml    │
   │ (Room+Cipher)  │  │ (Encrypted prefs)│  │  security  │  │ (TFLite+OCR) │
   └────────────────┘  └──────────────────┘  └────────────┘  └──────────────┘

   ┌────────────────┐  ┌──────────────────┐
   │ :benchmark     │  │ :baselineprofile │  (separate app modules for perf)
   └────────────────┘  └──────────────────┘
```

### 4.3 Dependency rules (enforced by Gradle + lint)

| From | Can depend on | Cannot depend on |
|---|---|---|
| `:app` | All `:feature:*`, all `:data:*`, all `:core:*` | — |
| `:feature:*` | `:core:domain`, `:core:common`, `:core:ui`, `:core:design-system` | Other `:feature:*`, `:data:*`, `:core:database`, `:core:security` |
| `:data:*` | `:core:database`, `:core:datastore`, `:core:domain`, `:core:common` | `:feature:*`, `:app` |
| `:core:domain` | Pure Kotlin only | Anything Android |
| `:core:common` | Kotlin stdlib | Anything Android (ideally) |
| `:core:ui`, `:core:design-system` | `:core:common`, AndroidX UI | Domain, data, features |
| `:core:database` | `:core:common`, `:core:security` | Features, domain leaking into SQL |
| `:core:security` | Android Keystore APIs only | — |
| `:core:ml` | `:core:common`, TFLite | Features, data |

### 4.4 Module manifest

| Module | Purpose | Type |
|---|---|---|
| `:app` | Application entry, manifest merge, nav host, DI entry point | `com.android.application` |
| `:feature:home` | Home dashboard screen | Android library + Compose |
| `:feature:transactions` | Transaction list & detail | Android library + Compose |
| `:feature:add` | Add/edit transaction | Android library + Compose |
| `:feature:stats` | Charts, insights | Android library + Compose |
| `:feature:sms-review` | Parsed-SMS review queue | Android library + Compose |
| `:feature:settings` | Settings, biometric, categories, export | Android library + Compose |
| `:feature:onboarding` | First-launch flow, permissions | Android library + Compose |
| `:data:transaction` | Transaction repository implementation | Android library |
| `:data:sms` | SMS receiver, historical scanner, parser rules | Android library |
| `:data:category` | Category + budget repository | Android library |
| `:core:domain` | Models, use cases, repository interfaces | Pure Kotlin (JVM) |
| `:core:common` | `Result`, dispatchers, `DomainError`, utils | Pure Kotlin (JVM) |
| `:core:database` | Room entities, DAOs, SQLCipher glue, migrations | Android library |
| `:core:datastore` | `EncryptedSharedPreferences` wrapper, typed keys | Android library |
| `:core:security` | `KeystoreManager`, `BiometricLock`, `CryptoHelpers` | Android library |
| `:core:design-system` | M3 theme, tokens, shared composables, icons | Android library + Compose |
| `:core:ui` | Error mappers, snackbar host, common UI utils | Android library + Compose |
| `:core:ml` | TFLite interpreter wrapper, Tesseract wrapper | Android library |
| `:core:testing` | Test fakes, fixtures, rules | Android library (testImpl) |
| `:benchmark` | Macrobenchmarks | `com.android.test` |
| `:baselineprofile` | Baseline profile generator | `com.android.test` |

### 4.5 Root project layout

```
paisavault/
├── .github/
│   ├── workflows/               # CI pipelines
│   ├── ISSUE_TEMPLATE/
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── dependabot.yml
├── app/
├── feature/
│   ├── home/
│   ├── transactions/
│   ├── add/
│   ├── stats/
│   ├── sms-review/
│   ├── settings/
│   └── onboarding/
├── data/
│   ├── transaction/
│   ├── sms/
│   └── category/
├── core/
│   ├── common/
│   ├── domain/
│   ├── database/
│   ├── datastore/
│   ├── security/
│   ├── design-system/
│   ├── ui/
│   ├── ml/
│   └── testing/
├── benchmark/
├── baselineprofile/
├── build-logic/
│   └── convention/              # Convention plugins
│       └── src/main/kotlin/
├── config/
│   ├── detekt/detekt.yml
│   └── ktlint/
├── docs/
│   ├── adr/                     # Architecture Decision Records
│   ├── tdd/                     # Technical Design Docs
│   └── diagrams/
├── fastlane/
│   └── metadata/android/        # Store listing for F-Droid
├── ml-training/                 # Python side project
├── schemas/                     # Exported Room schemas
├── scripts/                     # Dev scripts
├── gradle/
│   ├── libs.versions.toml
│   └── verification-metadata.xml
├── .editorconfig
├── .gitattributes
├── .gitignore
├── CHANGELOG.md
├── CODEOWNERS
├── CODE_OF_CONDUCT.md
├── CONTRIBUTING.md
├── LICENSE
├── NOTICE
├── PRIVACY.md
├── README.md
├── SECURITY.md
├── BUILD.md                     # this file
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 5. Build Infrastructure

### 5.1 Convention plugins

Located in `build-logic/convention/`. Registered in each module's `build.gradle.kts` by id. Single source of truth for shared build config.

**`build-logic/convention/build.gradle.kts`**
```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "paisavault.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "paisavault.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "paisavault.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidCompose") {
            id = "paisavault.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("jvmLibrary") {
            id = "paisavault.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("hilt") {
            id = "paisavault.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("room") {
            id = "paisavault.room"
            implementationClass = "RoomConventionPlugin"
        }
        register("lint") {
            id = "paisavault.lint"
            implementationClass = "LintConventionPlugin"
        }
    }
}
```

**`AndroidLibraryConventionPlugin.kt`** (sample)
```kotlin
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.android")
            apply("paisavault.lint")
        }
        extensions.configure<LibraryExtension> {
            compileSdk = 35
            defaultConfig {
                minSdk = 28
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                vectorDrawables.useSupportLibrary = true
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures { buildConfig = false }
        }
        configureKotlin()
        configureDetekt()
        dependencies {
            add("implementation", libs.findLibrary("kotlinx.coroutines.core").get())
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("turbine").get())
        }
    }
}
```

Feature modules apply `paisavault.android.feature` which layers Compose, Hilt, and domain + common deps on top of library.

### 5.2 `gradle.properties` (performance-tuned)

```properties
# JVM
org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC -Dfile.encoding=UTF-8
# Parallelism
org.gradle.parallel=true
org.gradle.configureondemand=true
# Caching
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.configuration-cache.problems=warn
# Kotlin
kotlin.code.style=official
kotlin.incremental=true
kotlin.daemon.jvmargs=-Xmx2g
# Android
android.useAndroidX=true
android.nonTransitiveRClass=true
android.nonFinalResIds=true
android.enableJetifier=false
# KSP
ksp.incremental=true
# Compose
org.jetbrains.compose.experimental.uikit.enabled=false
```

### 5.3 `settings.gradle.kts` (plugin management + module inclusion)

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PaisaVault"

include(
    ":app",
    ":feature:home", ":feature:transactions", ":feature:add",
    ":feature:stats", ":feature:sms-review", ":feature:settings",
    ":feature:onboarding",
    ":data:transaction", ":data:sms", ":data:category",
    ":core:common", ":core:domain",
    ":core:database", ":core:datastore", ":core:security",
    ":core:design-system", ":core:ui", ":core:ml", ":core:testing",
    ":benchmark", ":baselineprofile"
)
```

### 5.4 Module `build.gradle.kts` (example — feature)

```kotlin
plugins {
    id("paisavault.android.feature")
    id("paisavault.android.compose")
}

android { namespace = "com.paisavault.feature.home" }

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.designSystem)
    implementation(projects.core.ui)
}
```

Typed project accessors via `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` in `settings.gradle.kts`.

### 5.5 Gradle dependency verification

Generate and commit checksum metadata:
```bash
./gradlew --write-verification-metadata sha256 --refresh-dependencies
```
File: `gradle/verification-metadata.xml` (committed). CI fails if checksums don't match.

---

## 6. Dependencies (Version Catalog)

`gradle/libs.versions.toml` — abridged; add as needed per module.

```toml
[versions]
agp = "8.7.0"
kotlin = "2.0.20"
ksp = "2.0.20-1.0.25"
compose-bom = "2024.10.00"
room = "2.6.1"
hilt = "2.52"
sqlcipher = "4.5.4"
coroutines = "1.9.0"
lifecycle = "2.8.6"
navigation = "2.8.2"
biometric = "1.2.0-alpha05"
tflite = "2.16.1"
tesseract4android = "4.7.0"
vico = "2.0.0-alpha.28"
work = "2.9.1"
datastore = "1.1.1"
security-crypto = "1.1.0-alpha06"
kotlinx-serialization = "1.7.3"
kotlinx-datetime = "0.6.1"
kotlinx-collections-immutable = "0.3.8"
timber = "5.0.1"
jank-stats = "1.0.0-beta01"
tracing = "1.2.0"
profileinstaller = "1.4.1"
uiautomator = "2.3.0"
benchmark-macro = "1.3.2"
baseline-profile = "1.3.2"
llama-cpp-android = "0.1.61"
detekt = "1.23.7"
ktlint-gradle = "12.1.1"
spotless = "6.25.0"
license-report = "0.9.8"

[libraries]
# Core
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.13.1" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
kotlinx-collections-immutable = { module = "org.jetbrains.kotlinx:kotlinx-collections-immutable", version.ref = "kotlinx-collections-immutable" }

# Lifecycle & ViewModel
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Compose
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-compose-material-iconsext = { module = "androidx.compose.material:material-icons-extended" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.9.2" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }

# Room + SQLCipher
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-room-paging = { module = "androidx.room:room-paging", version.ref = "room" }
androidx-room-testing = { module = "androidx.room:room-testing", version.ref = "room" }
sqlcipher = { module = "net.zetetic:android-database-sqlcipher", version.ref = "sqlcipher" }
androidx-sqlite-ktx = { module = "androidx.sqlite:sqlite-ktx", version = "2.4.0" }

# DI
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version = "1.2.0" }
hilt-work = { module = "androidx.hilt:hilt-work", version = "1.2.0" }

# Security
androidx-biometric = { module = "androidx.biometric:biometric-ktx", version.ref = "biometric" }
androidx-security-crypto = { module = "androidx.security:security-crypto", version.ref = "security-crypto" }

# DataStore
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }

# ML
tensorflow-lite = { module = "org.tensorflow:tensorflow-lite", version.ref = "tflite" }
tensorflow-lite-support = { module = "org.tensorflow:tensorflow-lite-support", version = "0.4.4" }
tesseract4android = { module = "cz.adaptech.tesseract4android:tesseract4android", version.ref = "tesseract4android" }
llama-cpp-android = { module = "io.github.ggerganov:llama-cpp-android", version.ref = "llama-cpp-android" }

# Background
androidx-work-runtime = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }

# Charts
vico-compose-m3 = { module = "com.patrykandpatrick.vico:compose-m3", version.ref = "vico" }

# Logging
timber = { module = "com.jakewharton.timber:timber", version.ref = "timber" }

# Performance
androidx-jank-stats = { module = "androidx.metrics:metrics-performance", version.ref = "jank-stats" }
androidx-tracing-ktx = { module = "androidx.tracing:tracing-ktx", version.ref = "tracing" }
androidx-profileinstaller = { module = "androidx.profileinstaller:profileinstaller", version.ref = "profileinstaller" }
androidx-benchmark-macro = { module = "androidx.benchmark:benchmark-macro-junit4", version.ref = "benchmark-macro" }
androidx-uiautomator = { module = "androidx.test.uiautomator:uiautomator", version.ref = "uiautomator" }

# Testing
junit = { module = "junit:junit", version = "4.13.2" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-test-ext-junit = { module = "androidx.test.ext:junit", version = "1.2.1" }
androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version = "3.6.1" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
turbine = { module = "app.cash.turbine:turbine", version = "1.1.0" }
kotest-assertions-core = { module = "io.kotest:kotest-assertions-core", version = "5.9.1" }
kotest-property = { module = "io.kotest:kotest-property", version = "5.9.1" }

# Plugin classpath helpers (used in build-logic)
android-gradlePlugin = { module = "com.android.tools.build:gradle", version.ref = "agp" }
kotlin-gradlePlugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
ksp-gradlePlugin = { module = "com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin", version.ref = "ksp" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
android-test = { id = "com.android.test", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint-gradle" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
license-report = { id = "com.jaredsburrows.license", version.ref = "license-report" }
baseline-profile = { id = "androidx.baselineprofile", version.ref = "baseline-profile" }
```

---

## 7. Quality Gates

Machines enforce style. Humans review design.

### 7.1 Linters & formatters

| Tool | Purpose | License | Runs |
|---|---|---|---|
| **ktlint** | Kotlin style | MIT | pre-commit + CI |
| **detekt** | Static analysis (complexity, smells, potential bugs) | Apache 2.0 | CI (fail on issues) |
| **Android Lint** | Android-specific checks | Apache 2.0 | CI (baseline maintained) |
| **Spotless** | Unified format | Apache 2.0 | pre-commit |
| **OWASP Dependency-Check** | CVE scan | Apache 2.0 | nightly CI |
| **custom lint rules** | Project rules (no `println`, no `Log.*`, enforce `Timber`) | — | CI |

### 7.2 Detekt config (`config/detekt/detekt.yml`) — key rules

```yaml
complexity:
  LongMethod: { threshold: 60 }
  ComplexMethod: { threshold: 15 }
  TooManyFunctions: { thresholdInFiles: 20 }

naming:
  FunctionNaming: { active: true }
  ClassNaming: { active: true }
  MatchingDeclarationName: { active: true }

style:
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2']
  WildcardImport: { active: true }
  UnnecessaryAbstractClass: { active: true }
  ReturnCount: { max: 3 }

performance:
  SpreadOperator: { active: true }
  UnnecessaryTemporaryInstantiation: { active: true }

potential-bugs:
  UnsafeCallOnNullableType: { active: true }
  LateinitUsage: { active: true }
```

### 7.3 Pre-commit hooks (via `lefthook` or `husky`)

`lefthook.yml`:
```yaml
pre-commit:
  parallel: true
  commands:
    ktlint:
      glob: "*.{kt,kts}"
      run: ./gradlew ktlintCheck --daemon
    detekt:
      glob: "*.{kt,kts}"
      run: ./gradlew detekt --daemon
    spotless:
      glob: "*.{kt,kts,md,yml,xml}"
      run: ./gradlew spotlessCheck --daemon
commit-msg:
  commands:
    conventional:
      run: npx @commitlint/cli --edit {1}
```

### 7.4 Custom lint rules to ship

| Rule | Enforced |
|---|---|
| `NoAndroidLogUsage` | Use Timber, not `android.util.Log` |
| `NoPrintlnUsage` | Never `println` in production code |
| `NoHardcodedStringsInUi` | All UI strings come from `R.string` |
| `ForbiddenImport:com.google.firebase.*` | No Firebase |
| `ForbiddenImport:com.google.mlkit.*` | No ML Kit |
| `ForbiddenImport:com.google.android.gms.*` | No Play Services |
| `RequireImmutableAnnotation` | Stable data classes used in Compose must be `@Immutable` |

### 7.5 PR gates (must pass to merge)

- [ ] `./gradlew ktlintCheck detekt lint`
- [ ] `./gradlew test`
- [ ] `./gradlew :app:lintRelease` (no new warnings)
- [ ] `./gradlew licenseReleaseReport` (generates, commits)
- [ ] `./gradlew checkFossCompliance`
- [ ] APK size increase ≤ 2% unless justified in PR body
- [ ] At least 1 reviewer approval (if team)
- [ ] Signed commits (`gpg` or `ssh`)

---

## 8. Repository Hygiene

Files at repo root (templates provided separately):

| File | Purpose |
|---|---|
| `README.md` | Project overview, install, quick start |
| `BUILD.md` | This file — source of truth for engineering |
| `CONTRIBUTING.md` | Setup, dev loop, PR rules, code style |
| `SECURITY.md` | How to report vulnerabilities privately |
| `PRIVACY.md` | What data is collected (nothing), processed, stored |
| `CODE_OF_CONDUCT.md` | Contributor Covenant v2.1 |
| `CHANGELOG.md` | Keep-a-Changelog format, auto-generated |
| `LICENSE` | GPL-3.0-or-later (recommended) or Apache-2.0 |
| `NOTICE` | Third-party licenses, auto-generated |
| `CODEOWNERS` | Review assignment |
| `.editorconfig` | Editor-agnostic format rules |
| `.gitattributes` | Line-ending handling |
| `.gitignore` | Android-standard |

`.github/`:
- `PULL_REQUEST_TEMPLATE.md`
- `ISSUE_TEMPLATE/bug_report.md`
- `ISSUE_TEMPLATE/feature_request.md`
- `ISSUE_TEMPLATE/security_report.md` (redirects to `SECURITY.md`)
- `workflows/ci.yml`, `workflows/release.yml`, `workflows/nightly.yml`
- `dependabot.yml`

---

## 9. Phase 0 — Foundations (1–2 weeks)

**Exit criteria:** empty multi-module project builds clean on CI, release APK signs, quality gates enforced, first ADR merged.

### 9.1 Repo scaffold
- [ ] Initialize repo, `.gitignore`, `.editorconfig`, `.gitattributes`
- [ ] Add `LICENSE` (GPL-3.0-or-later), `NOTICE` placeholder
- [ ] Add all markdown docs: `README.md`, `BUILD.md`, `CONTRIBUTING.md`, `SECURITY.md`, `PRIVACY.md`, `CODE_OF_CONDUCT.md`
- [ ] Add GitHub templates (PR, issues)

### 9.2 Gradle
- [ ] `settings.gradle.kts` with module inclusion + typed project accessors
- [ ] `gradle.properties` tuned
- [ ] `gradle/libs.versions.toml` with catalog
- [ ] `build-logic/convention/` with plugins wired
- [ ] Dependency verification: `./gradlew --write-verification-metadata sha256`

### 9.3 Modules (empty shells)
- [ ] Create all module directories from § 4.4
- [ ] Each has its `build.gradle.kts` applying the right convention plugin
- [ ] `:core:common` populated with `Result`, `DomainError`, `Dispatchers`, `Clock`
- [ ] `:core:design-system` populated with M3 theme, tokens
- [ ] `:core:domain` populated with empty `Transaction`, `Category` sealed classes

### 9.4 Quality gates
- [ ] `detekt.yml` at `config/detekt/`
- [ ] `ktlint` Gradle plugin applied via convention
- [ ] `spotless` applied
- [ ] Custom lint module with forbidden-import rules
- [ ] Pre-commit hooks (`lefthook install`)
- [ ] Commitlint config for Conventional Commits

### 9.5 CI
- [ ] `.github/workflows/ci.yml` (§ 28) running on every PR
- [ ] CI passes on empty project
- [ ] Badge in `README.md` showing CI status

### 9.6 Documentation
- [ ] `docs/adr/0000-adr-template.md`
- [ ] `docs/adr/0001-use-multi-module-architecture.md`
- [ ] `docs/adr/0002-use-sqlcipher-for-local-encryption.md`
- [ ] `docs/adr/0003-use-tensorflow-lite-over-mlkit.md`
- [ ] `docs/tdd/template.md`

### 9.7 Signing
- [ ] Generate release keystore (offline), back up encrypted copies to two physical locations
- [ ] Store keystore path + password in `~/.gradle/gradle.properties` (not in repo)
- [ ] Release build signs cleanly

---

## 10. Phase 1 — MVP (4–6 weeks)

**Goal:** manual-entry expense tracker with encrypted storage, charts, biometric lock, accessibility, i18n. No SMS yet.

### 10.1 Domain
- [ ] `:core:domain` models: `Transaction`, `Category`, `Budget`, `Money`, `DateRange`
- [ ] Use case interfaces: `AddTransactionUseCase`, `ObserveMonthlySpend`, `ObserveCategoryBreakdown`, `SetBudgetUseCase`
- [ ] `Money` value class with currency-aware formatting

### 10.2 Data
- [ ] `:core:database` — Room entities, DAOs, `PaisaDatabase`, schema export enabled
- [ ] `:core:database` — SQLCipher integration with Keystore-bound key
- [ ] `:data:transaction` — `TransactionRepositoryImpl` implementing domain interface
- [ ] `:data:category` — `CategoryRepositoryImpl`, default-category seeding
- [ ] Hilt modules binding impls to interfaces

### 10.3 Security
- [ ] `:core:security` — `KeystoreManager`, `DatabaseKeyProvider`, `BiometricLockManager`
- [ ] `FLAG_SECURE` on `MainActivity`
- [ ] 60-second auto-lock via `LifecycleObserver`
- [ ] Lock on task-switcher entry
- [ ] Manifest permissions, `allowBackup=false`, network security config

### 10.4 UI (per feature)
- [ ] `:feature:onboarding` — welcome, biometric setup, category review
- [ ] `:feature:home` — current-month total, recent 5 txns, FAB
- [ ] `:feature:add` — amount, category picker, date, note
- [ ] `:feature:transactions` — infinite-scroll list, filter, search
- [ ] `:feature:stats` — pie (category), bar (6-month), budget progress
- [ ] `:feature:settings` — biometric toggle, category manager, theme, about

Each screen follows the MVI contract (§ 14).

### 10.5 Accessibility (baseline — enforced every PR)
- [ ] Every interactive element has `contentDescription` or `Modifier.semantics`
- [ ] Touch target ≥ 48×48dp
- [ ] Color contrast ≥ 4.5:1 (WCAG AA) — `:core:design-system` tokens pre-validated
- [ ] Supports dynamic type up to 200%
- [ ] TalkBack runs through Home → Add → Stats without dead-ends
- [ ] Dark mode support
- [ ] Test on **Accessibility Scanner** (Play Store, free)

### 10.6 Internationalization (baseline)
- [ ] All strings externalized to `res/values/strings.xml`
- [ ] No concatenation — use parameterized strings
- [ ] Plurals via `<plurals>` resource
- [ ] Currency formatting via `java.text.NumberFormat.getCurrencyInstance(locale)`
- [ ] Date formatting via `kotlinx-datetime` + `java.time.format`
- [ ] RTL mirror review (flip locale to Arabic to verify)
- [ ] Language launch list: English only at v1.0; infrastructure ready for hi, ta, te, bn

### 10.7 Performance (Phase 1 budget)
- [ ] Cold start ≤ 600 ms (p95 on Pixel 6a)
- [ ] Scroll jank ≤ 1%
- [ ] APK size ≤ 15 MB
- [ ] Memory ≤ 120 MB peak
- [ ] Macrobenchmark covering cold start + home-screen render

### 10.8 Tests
- [ ] Unit: use cases, `Money` arithmetic, date utilities — 80% cov
- [ ] Integration: repository + in-memory Room — fakes, not mocks
- [ ] Compose: every screen has at least one UI test (render + happy-path tap)
- [ ] Screenshot tests (Paparazzi) for `:core:design-system` components
- [ ] Macrobenchmark baseline

### 10.9 Phase 1 release
- [ ] Tag `v0.1.0`, auto-changelog, GitHub Release with signed APK + SHA-256 + NOTICE

---

## 11. Phase 2 — SMS Automation (2–3 weeks)

### 11.1 Permissions
- [ ] Declare `READ_SMS`, `RECEIVE_SMS` in `:app` manifest
- [ ] `:feature:onboarding` — permission rationale screen explaining:
  - What we read, what we don't
  - Why it stays on the device
  - How to revoke later
- [ ] Graceful degradation: app is fully usable as manual-only without SMS permission

### 11.2 Data layer
- [ ] `:data:sms` — `SmsReceiver` (BroadcastReceiver)
- [ ] `:data:sms` — `SmsScanner` (ContentResolver for historical)
- [ ] `:data:sms` — `SmsParser` with rule registry
- [ ] `:data:sms` — WorkManager job for historical scan with progress `Flow`
- [ ] Deduplication strategy: reference ID or `(amount, date, merchant)` hash

### 11.3 Parser rules
See § 25 for full pattern library.
- [ ] HDFC, SBI, ICICI, Axis, Kotak debit/credit patterns
- [ ] PhonePe, GPay, Paytm UPI patterns
- [ ] ATM-withdrawal variant
- [ ] Refund/reversal detection
- [ ] Unit tests: ≥ 5 anonymized sample SMS per rule

### 11.4 Review queue
- [ ] `:feature:sms-review` — screen showing parsed-but-unconfirmed transactions
- [ ] Swipe-to-confirm, tap-to-edit, swipe-to-reject
- [ ] Setting: auto-confirm high-confidence parses (default off)

### 11.5 Edge cases
- [ ] Multi-part SMS reassembly
- [ ] Foreign currency (flag and store original + INR-estimated)
- [ ] Refunds (shown as negative against original category)
- [ ] Credit events (salary, dividends) — separate visual, excluded from "spend"

### 11.6 Security around parser
- [ ] Treat SMS body as hostile input (§ 23)
- [ ] Regex ReDoS-safe (tested with pathological inputs, time-bounded execution)
- [ ] Amount caps, string length caps
- [ ] Never `eval` any SMS content

### 11.7 Phase 2 release
- [ ] `v0.2.0`

---

## 12. Phase 3 — AI/ML Categorization (3–4 weeks)

### 12.1 Training (Python — `ml-training/`)
- [ ] Collect labeled merchants (`data/merchants.csv`): start with ~1k, grow from user corrections
- [ ] Baseline: TF-IDF + Logistic Regression (Apache 2.0 sklearn)
- [ ] Evaluation: 80/10/10 split, F1 score per category, confusion matrix
- [ ] Convert to TFLite via `tf.keras` equivalent or hand-port vectorizer to Kotlin
- [ ] `requirements.txt` pinned

### 12.2 Android (`:core:ml`)
- [ ] `MerchantCategorizer` — TFLite wrapper, runs on `Dispatchers.Default`
- [ ] Preprocessing: lowercase, strip noise tokens (UPI-, POS-, VPA:)
- [ ] Confidence threshold (default 0.6) — below threshold → "needs review"
- [ ] Hardcoded high-confidence rules as fast path (see § 26)
- [ ] Entity extraction via regex + optional small TFLite NER (no ML Kit)

### 12.3 Active learning
- [ ] Every user correction saved to `ml_training_samples` table (§ 24)
- [ ] Export anonymized training data to `:ml-training/data/` via ADB script
- [ ] User-facing "retrain on-device" toggle in advanced settings (v1.3+)

### 12.4 Tests
- [ ] Python: unit tests on preprocessing + model metrics
- [ ] Android: `MerchantCategorizer` returns expected category for fixture merchants
- [ ] Performance: inference ≤ 50 ms per merchant on Pixel 6a

### 12.5 Phase 3 release
- [ ] `v0.3.0`

---

## 13. Phase 4 — Insights & Polish (2–3 weeks)

### 13.1 Insights
- [ ] Rule-based monthly summary ("You spent 40% more on Food than last month")
- [ ] Anomaly flagging: transactions > 2σ above 90-day category mean
- [ ] Budget burn-rate alerts at 80% of limit

### 13.2 Receipt OCR (FOSS)
- [ ] `:core:ml` — `ReceiptOcr` using **Tesseract4Android**
- [ ] Bundle `eng.traineddata` in `assets/tessdata/` (Apache 2.0 from tessdata_fast)
- [ ] Camera intent via Activity Result API
- [ ] Preprocess (grayscale, threshold) — optional OpenCV-Android
- [ ] Run same parsing regex on OCR output

### 13.3 Export / backup
- [ ] CSV export — encrypted ZIP with Argon2id-derived key from user passphrase
- [ ] Full DB backup via Storage Access Framework
- [ ] Restore flow with passphrase + integrity verification
- [ ] Versioned backup format (header: magic bytes, version, KDF params, HMAC)

### 13.4 Natural language query (optional, experimental)
- [ ] `:core:ml` — `LlmQueryEngine` via **llama.cpp-android** (MIT)
- [ ] Model: **Phi-2** (MIT) or **Qwen2-0.5B-Instruct** (Apache 2.0)
- [ ] Prompt: schema-aware text → read-only SQL
- [ ] Sandboxed read-only view; `INSERT/UPDATE/DELETE/DROP` blocked
- [ ] Gated by feature flag + 4GB-RAM check
- [ ] One-time model download with pinned SHA-256

### 13.5 Phase 4 release (v1.0)
- [ ] `v1.0.0` — first stable
- [ ] F-Droid submission

---

## 14. State Management (UDF/MVI)

All screens follow Unidirectional Data Flow. No two-way binding, no shared mutable state.

### 14.1 Contract per screen

```kotlin
// :core:ui
interface UiState
interface UiEvent
interface UiEffect

abstract class MviViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<F>(Channel.BUFFERED)
    val effects: Flow<F> = _effects.receiveAsFlow()

    abstract fun onEvent(event: E)

    protected fun setState(reduce: S.() -> S) { _state.update(reduce) }
    protected fun sendEffect(effect: F) { viewModelScope.launch { _effects.send(effect) } }
}
```

### 14.2 Example — Add Transaction screen

```kotlin
data class AddTxnState(
    val amount: String = "",
    val selectedCategoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isSaving: Boolean = false,
    val validationError: AddTxnValidation? = null,
) : UiState

sealed interface AddTxnEvent : UiEvent {
    data class OnAmountChange(val value: String) : AddTxnEvent
    data class OnCategorySelect(val id: Long) : AddTxnEvent
    data class OnDateChange(val date: LocalDate) : AddTxnEvent
    data class OnNoteChange(val text: String) : AddTxnEvent
    data object OnSave : AddTxnEvent
}

sealed interface AddTxnEffect : UiEffect {
    data object NavigateBack : AddTxnEffect
    data class ShowError(val message: UiText) : AddTxnEffect
}
```

### 14.3 Composable collection (lifecycle-aware)

```kotlin
@Composable
fun AddTxnScreen(viewModel: AddTxnViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect -> /* navigate / show snackbar */ }
    }
    AddTxnContent(state = state, onEvent = viewModel::onEvent)
}
```

### 14.4 Screen state holders vs ViewModel

For simple screens, a **plain state holder** (Compose `remember` + `rememberCoroutineScope`) can replace ViewModel. Rule: if state must survive config change AND process death, use ViewModel + SavedStateHandle. Otherwise, state holder is enough.

---

## 15. Error Taxonomy

Sealed hierarchy. No leaking `Exception` to UI.

### 15.1 `Result` type (`:core:common`)

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Failure(val error: DomainError) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}
```

### 15.2 `DomainError`

```kotlin
sealed class DomainError(open val cause: Throwable? = null) {
    sealed class Storage : DomainError() {
        data class DatabaseCorrupt(override val cause: Throwable) : Storage()
        data class DiskFull(override val cause: Throwable) : Storage()
        data class EncryptionFailed(override val cause: Throwable) : Storage()
    }
    sealed class Sms : DomainError() {
        data object PermissionDenied : Sms()
        data class ParseFailed(val smsId: Long) : Sms()
    }
    sealed class Ml : DomainError() {
        data object ModelLoadFailed : Ml()
        data class InferenceFailed(override val cause: Throwable) : Ml()
    }
    sealed class Validation : DomainError() {
        data object AmountInvalid : Validation()
        data object CategoryRequired : Validation()
        data object DateInFuture : Validation()
    }
    data class Unknown(override val cause: Throwable) : DomainError(cause)
}
```

### 15.3 UI mapping (`:core:ui`)

```kotlin
@Composable
fun DomainError.asUiText(): UiText = when (this) {
    is DomainError.Storage.DiskFull -> UiText.StringResource(R.string.error_disk_full)
    is DomainError.Validation.AmountInvalid -> UiText.StringResource(R.string.error_amount_invalid)
    /* ... */
    else -> UiText.StringResource(R.string.error_unknown)
}
```

### 15.4 Crash handler

Global `Thread.setDefaultUncaughtExceptionHandler` writes an **encrypted** crash report to local file (never uploads). Surface in Settings → Advanced → Crash logs.

---

## 16. Navigation

### 16.1 Type-safe destinations (Compose Navigation 2.8 + kotlinx.serialization)

```kotlin
@Serializable data object HomeRoute
@Serializable data object TransactionsRoute
@Serializable data class AddTxnRoute(val editId: Long? = null)
@Serializable data object StatsRoute
@Serializable data object SettingsRoute
```

```kotlin
NavHost(navController, startDestination = HomeRoute) {
    composable<HomeRoute> { HomeScreen(onNavigateToAdd = { navController.navigate(AddTxnRoute()) }) }
    composable<AddTxnRoute> { backStack ->
        val route: AddTxnRoute = backStack.toRoute()
        AddTxnScreen(editId = route.editId)
    }
    /* ... */
}
```

### 16.2 Rules
- No string routes
- Arguments are `@Serializable` primitives or data classes
- Back stack contract documented per feature
- No deep linking into biometric-protected screens without re-auth

---

## 17. Accessibility

Enforced every PR, not a final polish step.

### 17.1 Checklist per screen

- [ ] Every `Icon`, `Image` has meaningful `contentDescription` or `null` for decorative
- [ ] Every `IconButton`, `Card` that's clickable has `Modifier.semantics { role = Role.Button }`
- [ ] Touch target ≥ 48dp (use `Modifier.minimumInteractiveComponentSize()`)
- [ ] TalkBack focus order matches visual reading order
- [ ] Live regions for loading and error states (`Modifier.semantics { liveRegion = LiveRegionMode.Polite }`)
- [ ] Form inputs have `labelFor` / associated labels
- [ ] Error messages announced to TalkBack
- [ ] Custom composables expose correct semantics (merge or clear as appropriate)

### 17.2 Dynamic type
- [ ] Support up to 200% font scale without layout break
- [ ] Use `sp` for all text sizes, never `dp`
- [ ] No fixed-height text containers

### 17.3 Color & contrast
- [ ] `:core:design-system` tokens pass WCAG AA (4.5:1 for normal text, 3:1 for large)
- [ ] Dark mode validated separately
- [ ] Color is never the only indicator (icons + text accompany status)

### 17.4 Testing
- [ ] Run **Accessibility Scanner** before each release
- [ ] TalkBack manual pass before each release
- [ ] Compose tests: assert `SemanticsProperties` on critical nodes

---

## 18. Internationalization

### 18.1 Rules
- No string literal in UI code; all via `stringResource(R.string.*)`
- Parameterized formats use `%1$s`, never concatenation
- Plurals via `<plurals>`, not `if (count == 1)`
- Numbers and currency via `NumberFormat.getCurrencyInstance(locale)`
- Dates via `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)`
- RTL-safe layouts (use `start`/`end`, not `left`/`right`)

### 18.2 Launch languages
- v1.0: English
- v1.1: Hindi (`hi`), Tamil (`ta`), Telugu (`te`), Bengali (`bn`), Marathi (`mr`)
- Translation workflow: strings.xml master, Weblate (AGPL, self-hostable) or manual community PRs

### 18.3 Locale-aware expense tracking
- Currency defaults to `Currency.getInstance(Locale.getDefault())`; user override in Settings
- Week starts on locale-defined first day
- `kotlinx-datetime` for timezone-correct aggregations

---

## 19. Theming & Design System

`:core:design-system` exposes one theme, many tokens.

### 19.1 Tokens

```kotlin
object PaisaTokens {
    object Color {
        val primary = Color(0xFF006C4B)
        val onPrimary = Color(0xFFFFFFFF)
        /* ... full M3 scheme */
    }
    object Spacing {
        val xs = 4.dp; val s = 8.dp; val m = 16.dp
        val l = 24.dp; val xl = 32.dp
    }
    object Typography { /* M3 type scale */ }
    object Shape { val small = RoundedCornerShape(8.dp); /* ... */ }
    object Elevation { val card = 1.dp; val dialog = 6.dp }
    object Motion { val short = 150.ms; val medium = 300.ms }
}
```

### 19.2 Theme

```kotlin
@Composable
fun PaisaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= 31,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(colorScheme = colorScheme, typography = PaisaTypography, content = content)
}
```

### 19.3 Shared composables
`PaisaButton`, `PaisaTextField`, `PaisaCard`, `PaisaDialog`, `ErrorState`, `EmptyState`, `LoadingState`, `MoneyText` — all with screenshot tests in `:core:design-system`.

---

## 20. Performance Engineering

### 20.1 Budgets (enforced in CI)

| Metric | Target | Enforcement |
|---|---|---|
| Cold start (p95, Pixel 6a emulator) | ≤ 500 ms at v1.0 | macrobenchmark |
| Warm start (p95) | ≤ 200 ms | macrobenchmark |
| Scroll jank | ≤ 1% frames | JankStats + macrobenchmark |
| APK size (release, with baseline profile) | ≤ 20 MB | CI job fails on > 2% increase |
| Memory peak (typical session) | ≤ 150 MB | Profiler manual check + CI heap budget |
| DB query p95 (month transactions) | ≤ 50 ms | Instrumented test |
| ML inference per merchant | ≤ 50 ms | Instrumented benchmark |

### 20.2 Baseline Profile
- `:baselineprofile` module generates profile on emulator via `BaselineProfileGenerator`
- Profile regenerated every release (CI `release` workflow)
- Committed to `:app/src/main/baseline-prof.txt`

### 20.3 Macrobenchmarks (`:benchmark`)

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test fun coldStartup() = rule.measureRepeated(
        packageName = "com.paisavault",
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

### 20.4 Compose performance
- [ ] Enable Compose Compiler Metrics → inspect `app-release-module-metrics.json` each build
- [ ] Stable: < 1% unstable parameters target
- [ ] `@Immutable` on data classes used as Compose params
- [ ] `@Stable` on interfaces where appropriate
- [ ] Use `kotlinx.collections.immutable` for list params (not `List<T>` directly)
- [ ] `key()` in `LazyColumn` items
- [ ] Avoid `remember { mutableStateOf(...) }` lambdas captured in parent recomposition scope

### 20.5 Cold start attribution
- [ ] `androidx.tracing` sections around Hilt init, Room open, ML model load
- [ ] View traces in **Perfetto** (FOSS)

### 20.6 Memory
- [ ] LeakCanary in debug builds (Apache 2.0)
- [ ] Heap dumps on OOM in release → encrypted local file

---

## 21. Observability (local-only)

Everything stays on device. No network egress. Ever.

### 21.1 Logging
- **Timber** with:
  - `Timber.DebugTree()` in debug builds
  - No tree in release (logs no-op)
  - Custom `EncryptedFileTree` in advanced-mode release (gated by setting) — appends to `logs/app.enc` with AES-GCM

### 21.2 Metrics
- **JankStats** collects frame timing per screen → aggregated locally in `metrics.enc`
- Startup attribution via `androidx.tracing`
- User-facing "Performance" screen in Advanced Settings shows local aggregates

### 21.3 Crash handling
- Global uncaught handler → encrypted local file (max 10 entries, rolling)
- Stack traces ProGuard-deobfuscated in-app via bundled `mapping.txt` (debug) or user-initiated symbolication (release)

### 21.4 Opt-in telemetry share
- User can manually export the encrypted log file via Settings → Support → Export diagnostics
- Only way data ever leaves the device

---

## 22. Feature Flags

Local-only, no remote config (that would violate local-first).

### 22.1 Flag definitions (`:core:common`)

```kotlin
enum class FeatureFlag(val default: Boolean, val requiresRestart: Boolean = false) {
    EXPERIMENTAL_LLM_QUERY(default = false, requiresRestart = true),
    EXPERIMENTAL_OCR(default = false),
    ADVANCED_LOGGING(default = false, requiresRestart = true),
    ANOMALY_DETECTION(default = true),
}
```

### 22.2 Backing store
- `:core:datastore` — `FeatureFlagsRepository` over DataStore Preferences
- Exposes `Flow<Map<FeatureFlag, Boolean>>`
- Settings → Advanced → Feature Flags toggles each at runtime

### 22.3 Usage in Compose
```kotlin
val llmEnabled by featureFlags.observe(FeatureFlag.EXPERIMENTAL_LLM_QUERY).collectAsStateWithLifecycle(initialValue = false)
if (llmEnabled) { LlmQueryButton() }
```

---

## 23. Security Implementation

Retained from v1 plan plus MAANG-grade additions. See audit in previous iteration for full threat model — summary here.

### 23.1 Data at rest
- SQLCipher (AES-256) with Keystore-bound 256-bit key
- `EncryptedSharedPreferences` for typed preferences
- Backup/export encrypted with user-passphrase (**Argon2id** KDF, params: t=3, m=64MiB, p=4)

### 23.2 App access
- `BiometricPrompt` with `BIOMETRIC_STRONG` only; device-credential fallback
- `FLAG_SECURE` on `MainActivity`
- 60-second background auto-lock via `LifecycleObserver`

### 23.3 Manifest
See § 9 of v1 (carried forward). Additions:
- `android:enableOnBackInvokedCallback="true"` (Android 13+ back gesture)
- `android:dataExtractionRules` + `android:fullBackupContent` exclusions
- `android:hasFragileUserData="true"` (uninstall confirmation)

### 23.4 Build hardening
- `isMinifyEnabled = true`, `isShrinkResources = true`
- R8 full mode
- APK signature scheme v3 (default AGP 8+)
- Release keystore 4096-bit RSA, 25-year validity
- `android:debuggable=false` verified per release
- No `INTERNET` permission until genuinely needed

### 23.5 Input validation
- SMS body treated as untrusted; amount caps, length caps, ReDoS-safe regex with timeouts
- Room parameterized queries only (no `@RawQuery` with concatenation)

### 23.6 Supply chain
- Gradle dependency verification (checksums committed)
- OWASP Dependency-Check in nightly CI
- Renovate for automated updates
- Signed commits required

### 23.7 Pre-release security verification
- [ ] `apksigner verify --verbose app-release.apk`
- [ ] `apkanalyzer manifest permissions` — only expected perms
- [ ] `apkanalyzer dex packages` — no Firebase / GMS / ML Kit
- [ ] MobSF static scan
- [ ] `adb backup` test returns empty
- [ ] Rooted-device smoke test

---

## 24. Database & Migrations

### 24.1 Schema export

```kotlin
@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, BudgetEntity::class,
                SmsRuleEntity::class, MlSampleEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PaisaDatabase : RoomDatabase() { /* ... */ }
```

Exported to `schemas/com.paisavault.core.database.PaisaDatabase/1.json`. Committed to repo.

### 24.2 Migrations
- Auto-migration preferred when safe
- Manual `Migration` classes for non-trivial changes — **each migration has a test**

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN ml_confidence REAL DEFAULT NULL")
    }
}
```

### 24.3 Test
```kotlin
@Test fun migrate1To2() {
    helper.createDatabase(TEST_DB, 1).use { /* insert v1 fixture */ }
    helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    /* verify data preserved */
}
```

### 24.4 Entities
Carried from v1 plan (§ 10). Key indexes documented per table.

### 24.5 DB views for hot queries
```kotlin
@DatabaseView("""
    SELECT t.categoryId, SUM(t.amount) AS total, strftime('%Y-%m', t.date/1000, 'unixepoch') AS month
    FROM transactions t
    WHERE t.type = 'DEBIT'
    GROUP BY month, t.categoryId
""", viewName = "monthly_category_spend")
data class MonthlyCategorySpend(val categoryId: Long, val total: Double, val month: String)
```

---

## 25. SMS Parsing Strategy

Full rule-based parser with extensive test corpus. See v1 BUILD.md § 11 for sender patterns and sample rule implementation — carried forward verbatim, additions below.

### 25.1 Rule architecture (updated)

```kotlin
interface SmsParserRule {
    val id: String
    val priority: Int
    fun matches(sender: String, body: String): Boolean
    fun parse(sender: String, body: String, timestamp: Long): ParseResult
}

sealed interface ParseResult {
    data class Parsed(val txn: ParsedTxn) : ParseResult
    data class Rejected(val reason: String) : ParseResult
}
```

### 25.2 Rule registry (DataStore + in-memory)
- Rules shipped as defaults in `:data:sms/assets/rules.json`
- Users can enable/disable per sender
- Advanced: add custom rules via Settings (v1.2+)

### 25.3 Test corpus
- `:data:sms/src/test/resources/sample_sms/*.txt` — anonymized real SMS per bank
- Property-based fuzz tests via **Kotest Property** (Apache 2.0)

---

## 26. ML Pipeline

Carried from v1 (§ 12). Additions for MAANG grade:

### 26.1 Reproducibility
- `ml-training/requirements.txt` pinned
- `ml-training/train.py` takes `--seed` arg, deterministic
- Training artifacts (model, metrics, confusion matrix) versioned in `ml-training/artifacts/v{N}/`
- Model SHA-256 hashed and pinned in `:core:ml/assets/model.meta.json`

### 26.2 Model card
`ml-training/MODEL_CARD.md` documents:
- Training data source, size, label distribution
- Evaluation metrics (accuracy, F1 per category, confusion)
- Known biases (e.g., stronger on food/transport than rare categories)
- Intended use, out-of-scope use
- License of weights

### 26.3 A/B testing on-device
- Two model versions can coexist (`model_v1.tflite`, `model_v2.tflite`)
- Feature flag controls which runs
- Categorization result carries `model_version` for later comparison

---

## 27. Testing Strategy

Test pyramid, enforced coverage, diverse test types.

### 27.1 Pyramid

```
              ┌──────────────┐
              │   E2E (UI)   │   ~5%  — happy path per feature
              └──────────────┘
           ┌──────────────────────┐
           │  Integration (fakes) │   ~20% — repos, use cases
           └──────────────────────┘
      ┌────────────────────────────────┐
      │      Unit (pure Kotlin)        │   ~75% — domain, parsers, utils
      └────────────────────────────────┘
      Plus: screenshot, macrobench, fuzz, property
```

### 27.2 Tools
- **JUnit 4** for Android tests, **Kotest** for property & assertions
- **Turbine** for Flow testing
- **Paparazzi** (Apache 2.0) for screenshot tests — no emulator needed
- **Macrobenchmark** for perf
- **MockK** only where fakes don't fit (prefer fakes)
- **Robolectric** avoided — prefer pure JVM or instrumented

### 27.3 Coverage targets
- `:core:domain`, `:core:common` — 90%
- `:data:*` — 80%
- `:feature:*` — 60% (UI tests cover rest)

### 27.4 Robot pattern for UI tests

```kotlin
class AddTxnRobot(private val composeRule: ComposeTestRule) {
    fun enterAmount(value: String) = apply { composeRule.onNodeWithTag("amount").performTextInput(value) }
    fun selectCategory(name: String) = apply { /* ... */ }
    fun tapSave() = apply { composeRule.onNodeWithTag("save").performClick() }
    fun assertErrorShown() = apply { composeRule.onNodeWithTag("error").assertIsDisplayed() }
}
```

### 27.5 Fake-first
`:core:testing` exposes `FakeTransactionRepository`, `FakeCategoryRepository`, `FakeClock`, `FakeCrypto`. Integration tests use fakes; only final UI tests use real Room in-memory.

---

## 28. CI/CD Pipeline

### 28.1 `.github/workflows/ci.yml`

```yaml
name: CI
on:
  pull_request: { branches: [main] }
  push: { branches: [main] }

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew ktlintCheck detekt spotlessCheck

  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew testDebugUnitTest
      - uses: actions/upload-artifact@v4
        if: always()
        with: { name: unit-test-reports, path: '**/build/reports/tests/' }

  instrumented-test:
    runs-on: ubuntu-latest
    strategy:
      matrix: { api-level: [28, 34] }
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: reactivecircus/android-emulator-runner@v2
        with:
          api-level: ${{ matrix.api-level }}
          script: ./gradlew connectedDebugAndroidTest

  android-lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :app:lintRelease
      - uses: actions/upload-artifact@v4
        with: { name: lint-report, path: app/build/reports/lint-results-release.html }

  foss-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew checkFossCompliance licenseReleaseReport

  apk-size:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew :app:assembleRelease
      - name: Check size budget
        run: |
          size=$(stat -c%s app/build/outputs/apk/release/app-release.apk)
          budget=$((20 * 1024 * 1024))
          if [ $size -gt $budget ]; then echo "APK exceeds 20 MB budget: $size bytes"; exit 1; fi

  dependency-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: dependency-check/Dependency-Check_Action@main
        with: { project: 'PaisaVault', path: '.', format: 'HTML' }
```

### 28.2 `.github/workflows/release.yml`

```yaml
name: Release
on:
  push: { tags: ['v*.*.*'] }

jobs:
  release:
    runs-on: ubuntu-latest
    permissions: { contents: write }
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 0 }
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4

      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > release.keystore

      - name: Build release APK
        env:
          STORE_FILE: ../release.keystore
          STORE_PASSWORD: ${{ secrets.STORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew :app:assembleRelease

      - name: Generate baseline profile
        run: ./gradlew :app:generateBaselineProfile

      - name: Generate license report
        run: ./gradlew licenseReleaseReport

      - name: Checksums
        run: sha256sum app/build/outputs/apk/release/app-release.apk > checksums.txt

      - name: Generate changelog
        uses: orhun/git-cliff-action@v4
        with: { args: --latest --strip header }

      - uses: softprops/action-gh-release@v2
        with:
          files: |
            app/build/outputs/apk/release/app-release.apk
            checksums.txt
            app/build/reports/licenses/licenseReleaseReport.html
          generate_release_notes: true
```

### 28.3 `.github/workflows/nightly.yml`
- Full OWASP Dependency-Check
- MobSF static scan
- Firebase Test Lab smoke tests (if used — note: Firebase services are proprietary; optional)
- Notify on failure (local-only options: file an issue automatically via `gh` CLI)

### 28.4 `dependabot.yml` (or Renovate)

```yaml
version: 2
updates:
  - package-ecosystem: gradle
    directory: /
    schedule: { interval: weekly }
    open-pull-requests-limit: 5
    groups:
      androidx: { patterns: ['androidx.*'] }
      kotlin: { patterns: ['org.jetbrains.kotlin*'] }
```

---

## 29. Release Engineering

### 29.1 Versioning
- **Semantic Versioning 2.0** — `MAJOR.MINOR.PATCH`
- `versionCode` = monotonically increasing integer, derived from git tag count

### 29.2 Conventional Commits
- `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `build:`, `ci:`, `perf:`, `chore:`
- Breaking changes: `feat!:` or `BREAKING CHANGE:` in body
- Enforced by commitlint pre-commit

### 29.3 Changelog
- Auto-generated via **git-cliff** (MIT) at release time
- Follows **Keep a Changelog** format
- Committed to `CHANGELOG.md`

### 29.4 Release runbook

```
1. Ensure main is green (CI passing)
2. Create release branch: release/vX.Y.Z
3. Update versionName in :app/build.gradle.kts
4. Run ./gradlew dependencyUpdates → resolve any must-have
5. Run full test suite + manual QA pass on physical device
6. Update PRIVACY.md if data handling changed
7. Generate baseline profile (CI will redo, but verify locally)
8. Merge release branch to main via PR
9. Tag: git tag -s -a vX.Y.Z -m "Release vX.Y.Z"
10. Push tag: git push origin vX.Y.Z
11. Release workflow runs automatically
12. Verify GitHub Release assets: APK, checksum, license report, NOTICE
13. Announce: GitHub Discussion, relevant forums
14. If F-Droid: submit metadata PR to fdroiddata
```

### 29.5 Rollback
- Users on a bad version: publish hotfix immediately (vX.Y.Z+1), don't attempt remote kill-switch (would need server)
- Include in-app update check against GitHub Releases API (read-only, no data sent)

### 29.6 Keystore management
- **Primary**: YubiKey 5 or equivalent hardware token OR offline USB drive kept in safe
- **Backup 1**: Encrypted copy on separate USB, different physical location
- **Backup 2**: Paper backup (Shamir Secret Sharing via `ssss`, GPL-3.0) split across trusted holders
- **Rotation**: N/A for app signing key (Android requires same key for app updates). If compromised → Play Key Upgrade or accept uninstall-reinstall migration (document path for users)

---

## 30. Disaster Recovery

### 30.1 Scenarios & plans

| Scenario | Plan |
|---|---|
| Keystore lost | Publish app under new package name, migrate users via export-import |
| Keystore leaked | Same — rotation not possible for in-place Android app updates |
| Signing password forgotten | Recover from paper/Shamir backup |
| DB corrupted on user's device | User restore from latest encrypted backup; if none, data loss |
| User forgets passphrase (backup) | No recovery (zero-knowledge); documented clearly |
| Repository taken down | Rebuild from local clones; CI reproducible from scratch |
| Developer machine lost | Re-clone, re-install; keystore from offline backup |
| Breach (theoretical) | Since no server, breach = local device compromise. Doc remediation steps for users |
| Malicious dependency version published | Gradle dep verification catches checksum mismatch → CI fails |

### 30.2 User-facing documentation
`docs/user/disaster-recovery.md` explains to end users:
- How to create encrypted backups
- Where to store them
- How to restore
- What to do if device is lost

---

## 31. Documentation Practice

### 31.1 ADRs (`docs/adr/`)
- Format: **MADR 3.0** (https://adr.github.io/madr/)
- One file per decision, numbered
- Statuses: Proposed → Accepted → Superseded
- Every major tech choice gets an ADR

**Seed ADRs (write during Phase 0):**
1. `0001-use-multi-module-architecture.md`
2. `0002-use-sqlcipher-for-local-encryption.md`
3. `0003-use-tensorflow-lite-over-mlkit.md`
4. `0004-use-compose-navigation-type-safe.md`
5. `0005-use-mvi-for-screen-state.md`
6. `0006-use-tesseract-for-ocr.md`
7. `0007-foss-only-policy.md`

### 31.2 TDDs (`docs/tdd/`)
- One file per non-trivial feature before implementation
- Template covers: Problem, Goals, Non-Goals, Design, Alternatives, Risks, Rollout, Testing

**Seed TDDs:**
1. `sms-parser.md`
2. `ml-categorizer.md`
3. `encrypted-backup-format.md`
4. `biometric-lock-lifecycle.md`

### 31.3 KDoc standards
- Public APIs must have KDoc
- KDoc states *what* and *why*, not *how*
- `@throws`, `@return`, `@param` where applicable
- `Dokka` (Apache 2.0) generates HTML docs → published to GitHub Pages

### 31.4 In-code comments
- Minimal — code should be self-evident from names
- Comments explain non-obvious decisions, not code mechanics

---

## 32. iOS Planning & KMP Evaluation

### 32.1 Hard constraint
iOS forbids third-party SMS reading. The core feature is impossible on iOS by design.

### 32.2 Viable iOS scope
- Manual entry
- Receipt OCR (Vision framework or Tesseract via CocoaPods)
- CSV import from Android backup
- Apple Pay Wallet transaction import
- Share-sheet extension: user picks SMS text → parse per-message

### 32.3 Kotlin Multiplatform evaluation

| Layer | KMP feasibility | Decision |
|---|---|---|
| `:core:domain` | 100% pure Kotlin | Share via KMP |
| `:core:common` | 95% pure Kotlin | Share via KMP (with `expect/actual` for platform utils) |
| `:data:*` repos | Mostly pure Kotlin with SQLDelight (Apache 2.0) | Share via KMP; replace Room with SQLDelight |
| `:core:security` | Keystore is Android-specific; iOS has Keychain | Re-implement behind common interface |
| `:core:ml` | TFLite runs on both (via platform bindings) | Share via KMP |
| `:feature:*` UI | Compose Multiplatform iOS is beta | Native SwiftUI per platform |
| `:data:sms` | Android-only | Not shared |

**Decision rule:** If iOS scope stays at ≤ 4 screens (manual entry + list + stats + settings), a **native SwiftUI rewrite sharing only `:core:domain` as a KMP module** is the lowest risk. If iOS scope grows, migrate `:data:*` to SQLDelight and share more.

### 32.4 Android prep for KMP (do now, cost is small)
- `:core:domain` is already pure Kotlin
- Avoid Android imports in `:core:common` (use `expect/actual` if needed later)
- Use `kotlinx-datetime` not `java.time` in domain
- Use `kotlinx-serialization` not Gson/Moshi
- Avoid `Context` in repository interfaces

### 32.5 iOS kickoff (after Android v1.0)
- Swift Package Manager project
- Link KMP `:core:domain` as XCFramework
- SwiftUI with Observation framework
- Keychain + LocalAuthentication
- Core Data or GRDB (with SQLCipher for iOS)
- Core ML converted from TFLite via `coremltools`

---

## 33. Licensing & FOSS Compliance

Carried forward from v1 § 16. MAANG additions:

### 33.1 License report in CI
`com.jaredsburrows.license` (Apache 2.0) Gradle plugin runs every build, emits HTML report. Pre-release workflow fails if any new dependency has non-approved license.

### 33.2 FOSS compliance Gradle task
See v1 snippet. Maintained in `build-logic/convention/`. Blocked groups:
```kotlin
val blockedGroups = setOf(
    "com.google.android.gms",
    "com.google.firebase",
    "com.google.mlkit",
)
```

### 33.3 SPDX headers
Every `.kt`/`.kts` file starts with:
```kotlin
// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors
```
Enforced via Spotless.

### 33.4 NOTICE file
Generated at release. Published in-app via Settings → About → Licenses (uses `AboutLibraries` library — Apache 2.0, fully FOSS).

---

## 34. Development Cadence

Realistic timeline for solo part-time (10–15 hrs/week):

| Phase | Calendar | Outcome |
|---|---|---|
| Learning (Kotlin + Compose + Room) | 2 weeks | Able to read + write basics |
| **Phase 0 — Foundations** | 1–2 weeks | Empty project, CI green, first ADRs |
| **Phase 1 — MVP** | 4–6 weeks | `v0.1.0` — manual tracker, a11y, i18n |
| **Phase 2 — SMS** | 2–3 weeks | `v0.2.0` |
| **Phase 3 — ML** | 3–4 weeks | `v0.3.0` |
| **Phase 4 — Polish** | 2–3 weeks | `v1.0.0`, F-Droid submission |
| **Total to v1.0** | **~14–20 weeks** | |

Full-time (40 hrs/week): roughly half.

### Deliverable gates per phase

Each phase ends with ALL of:
- [ ] Feature work complete
- [ ] Tests passing (coverage target met)
- [ ] Perf budget met in macrobenchmark
- [ ] A11y pass with Accessibility Scanner
- [ ] Manual QA on physical device (Android 9 + 14)
- [ ] ADRs for new decisions
- [ ] CHANGELOG updated
- [ ] Signed release APK + SHA-256 published

---

## Open Questions to Resolve Before Phase 0

1. **Banks**: which Indian banks/UPI apps do you actually use? (Parser priority order)
2. **Phone**: Android version of your test device?
3. **License**: GPL-3.0-or-later (recommended, keeps derivatives open) or Apache-2.0?
4. **GitHub**: repo name confirmed as `Expense-Tracker`?
5. **First milestone target date**: what's realistic for v0.1.0?

Answer these and Phase 0 starts immediately.
