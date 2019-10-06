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
import groovy.util.Node
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
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

        // TODO: See `gradle-plugin-development` for what needs to be done for the shadow plugin to correctly work with plugin dev
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
        val compileOnly = configurations.getByName("compileOnly")
        compileOnly.extendsFrom(shaded)
        return shaded
    }

    private
    fun Project.configureShadowJarTask(shadedConfiguration: Configuration, shadedExtension: ShadedArtifactExtension): TaskProvider<ShadowJar> {
        // TODO: Remove once we can properly differ, maybe use relocate(Relocator) with custom Relocator implementation
        afterEvaluate {
            tasks.named<ShadowJar>("shadowJar") {
                for (pkg in shadedExtension.packagesToRelocate.get()) {
                    relocate(pkg, "${shadedExtension.relocatePackagePrefix.get()}.$pkg")
                }
                exclude("module-info.class")
            }
        }

        return tasks.named<ShadowJar>("shadowJar") {
            classifier = null
            configurations = listOf(shadedConfiguration)
            mergeServiceFiles()
        }
    }

    private
    fun Project.wireShadowJarTaskInLifecycle(shadowJar: TaskProvider<ShadowJar>) {
        gradle.taskGraph.whenReady {
            if (hasTask(tasks.getByName("assemble"))) {
                tasks.named<Jar>("jar") {
                    enabled = false
                }
            }
        }
        tasks.named("assemble") {
            dependsOn(shadowJar)
        }
    }
}