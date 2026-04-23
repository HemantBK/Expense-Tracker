pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "PaisaVault"

include(
    ":app",
    ":feature:home",
    ":feature:transactions",
    ":feature:add",
    ":feature:stats",
    ":feature:sms-review",
    ":feature:settings",
    ":feature:onboarding",
    ":data:transaction",
    ":data:sms",
    ":data:category",
    ":core:common",
    ":core:domain",
    ":core:database",
    ":core:datastore",
    ":core:security",
    ":core:design-system",
    ":core:ui",
    ":core:ml",
    ":core:testing",
    ":benchmark",
    ":baselineprofile",
)
