// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

import com.google.devtools.ksp.gradle.KspExtension
import com.paisavault.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx.room.runtime").get())
                add("implementation", libs.findLibrary("androidx.room.ktx").get())
                add("ksp", libs.findLibrary("androidx.room.compiler").get())
            }

            extensions.configure<KspExtension> {
                arg("room.schemaLocation", "${project.projectDir}/schemas")
                arg("room.generateKotlin", "true")
            }
        }
    }
}
