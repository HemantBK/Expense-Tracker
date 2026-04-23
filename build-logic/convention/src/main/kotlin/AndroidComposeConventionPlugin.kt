// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

import com.android.build.api.dsl.CommonExtension
import com.paisavault.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            val extension = extensions.getByType<CommonExtension<*, *, *, *, *, *>>()
            configureAndroidCompose(extension)
        }
    }

    private fun Project.configureAndroidCompose(
        commonExtension: CommonExtension<*, *, *, *, *, *>,
    ) {
        commonExtension.apply {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            val bom = libs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(bom))
            add("implementation", libs.findLibrary("androidx.compose.ui").get())
            add("implementation", libs.findLibrary("androidx.compose.material3").get())
            add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
            add("androidTestImplementation", platform(bom))
            add("androidTestImplementation", libs.findLibrary("androidx.compose.ui.test.junit4").get())
            add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())
        }
    }
}
