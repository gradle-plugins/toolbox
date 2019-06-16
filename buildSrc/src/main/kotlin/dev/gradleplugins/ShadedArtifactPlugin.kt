/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*

class ShadedArtifactPlugin: Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        applyShadowPlugin()
        val shadedConfiguration = createShadedConfiguration()
        val shadedArtifact = createShadedExtension()
        val shadowJar = configureShadowJarTask(shadedConfiguration, shadedArtifact)
        wireShadowJarTaskInLifecycle(shadowJar)
    }

    private
    fun Project.applyShadowPlugin() {
        apply<ShadowPlugin>()
    }

    private fun Project.createShadedExtension(): ShadedArtifactExtension {
        val result = extensions.create("shadedArtifact", ShadedArtifactExtension::class.java)
        result.packagesToRelocate.empty()
        result.relocatePackagePrefix.set(provider { "${group}.internal.impldep" })
        return result
    }

    private
    fun Project.createShadedConfiguration(): Configuration {
        val shaded by configurations.creating
        val implementation = configurations.getByName("implementation")
        implementation.extendsFrom(shaded)
        return shaded
    }

    private
    fun Project.configureShadowJarTask(shadedConfiguration: Configuration, shadedExtension: ShadedArtifactExtension): TaskProvider<ShadowJar> {
        return tasks.named<ShadowJar>("shadowJar") {
            classifier = null
            configurations = listOf(shadedConfiguration)
            mergeServiceFiles()
            for (pkg in shadedExtension.packagesToRelocate.get()) {
                relocate(pkg, "${shadedExtension.relocatePackagePrefix.get()}.$pkg")
            }
        }
    }

    private
    fun Project.wireShadowJarTaskInLifecycle(shadowJar: TaskProvider<ShadowJar>) {
        val jar: Jar by tasks
        jar.enabled = false
        tasks["assemble"].dependsOn(shadowJar)
    }
}