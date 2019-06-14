package dev.gradleplugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.CopyrightConfiguration
import org.jetbrains.gradle.ext.ProjectSettings
import java.io.File


object Apache2Copyright {
    val profileName = "ASL2"
    val keyword = "Copyright"
    val notice =
            """Copyright ${"$"}{today.year} the original author or authors.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License."""
}


open class IdePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        configureIdeaForRootProject()
    }

    private
    fun Project.configureIdeaForRootProject() {
        apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
        tasks.named("idea") {
            doFirst {
                throw RuntimeException("To import in IntelliJ, please follow the instructions here: https://github.com/gradle/gradle/blob/master/CONTRIBUTING.md#intellij")
            }
        }

        plugins.withType<IdeaPlugin> {
            with(model) {
                module {
                    excludeDirs = excludeDirs + rootExcludeDirs
                }

                project {
                    jdkName = "8.0"
                    wildcards.add("?*.gradle")
                    vcs = "Git"

                    settings {
                        configureCopyright()
                    }
                }
            }
        }
    }

    private
    fun ProjectSettings.configureCopyright() {
        copyright {
            useDefault = Apache2Copyright.profileName
            profiles {
                create(Apache2Copyright.profileName) {
                    notice = Apache2Copyright.notice
                    keyword = Apache2Copyright.keyword
                }
            }
        }
    }
}


private
val Project.rootExcludeDirs
    get() = setOf<File>(
            file("intTestHomeDir"),
            file("buildSrc/build"),
            file("buildSrc/.gradle"))


fun IdeaProject.settings(configuration: ProjectSettings.() -> kotlin.Unit) = (this as ExtensionAware).configure(configuration)

fun ProjectSettings.copyright(configuration: CopyrightConfiguration.() -> kotlin.Unit) = (this as ExtensionAware).configure(configuration)
