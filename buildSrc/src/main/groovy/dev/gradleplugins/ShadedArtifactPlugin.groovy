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

class ShadedArtifactPlugin implements Plugin<Project> {
    void apply(Project project) {
        applyShadowPlugin(project)
        def shadedConfiguration = createShadedConfiguration(project)
        def shadedArtifact = createShadedExtension(project)
        def shadowJar = configureShadowJarTask(project, shadedConfiguration, shadedArtifact)
        wireShadowJarTaskInLifecycle(project, shadowJar)

        // TODO: See `gradle-plugin-development` for what needs to be done for the shadow plugin to correctly work with plugin dev
    }

    private void applyShadowPlugin(Project project) {
        project.apply plugin: ShadowPlugin
    }

    private ShadedArtifactExtension createShadedExtension(Project project) {
        def result = project.extensions.create("shadedArtifact", ShadedArtifactExtension)
        result.packagesToRelocate.empty()
        result.relocatePackagePrefix.set(project.provider { "${project.group}.internal.impldep" })
        return result
    }

    private Configuration createShadedConfiguration(Project project) {
        def shaded = project.configurations.create("shaded")
        def compileOnly = project.configurations.getByName("compileOnly")
        compileOnly.extendsFrom(shaded)
        return shaded
    }

    private TaskProvider<ShadowJar> configureShadowJarTask(Project project, Configuration shadedConfiguration, ShadedArtifactExtension shadedExtension) {
        // TODO: Remove once we can properly differ, maybe use relocate(Relocator) with custom Relocator implementation
        project.afterEvaluate {
            project.tasks.named("shadowJar", ShadowJar) {
                for (pkg in shadedExtension.packagesToRelocate.get()) {
                    relocate(pkg, "${shadedExtension.relocatePackagePrefix.get()}.$pkg")
                }
                exclude("module-info.class")
            }
        }

        return project.tasks.named("shadowJar", ShadowJar) {
            classifier = null
            configurations = [shadedConfiguration]
            mergeServiceFiles()
        }
    }

    private TaskProvider<ShadowJar> wireShadowJarTaskInLifecycle(Project project, TaskProvider<ShadowJar> shadowJar) {
        project.gradle.taskGraph.whenReady { taskGraph ->
            if (taskGraph.hasTask(project.tasks.getByName("assemble"))) {
                project.tasks.named("jar", Jar) {
                    enabled = false
                }
            }
        }
        project.tasks.named("assemble") {
            dependsOn(shadowJar)
        }
        project.tasks.named("jar") {
            dependsOn(shadowJar)
        }
    }
}