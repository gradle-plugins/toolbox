package dev.gradleplugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import java.io.File

open class IdePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        configureIdeaForRootProject()
    }

    private
    fun Project.configureIdeaForRootProject() {
        pluginManager.withPlugin("org.jetbrains.gradle.plugin.idea-ext") {
            tasks.named("idea") {
                doFirst {
                    throw RuntimeException("To import in IntelliJ, please follow the instructions here: https://github.com/gradle/gradle/blob/master/CONTRIBUTING.md#intellij but use this repo ;-)")
                }
            }

            // TODO: Prompt on `idea` plugin id, the type is outside of our boundary
            plugins.withType<IdeaPlugin> {
                with(model) {
                    module {
                        // TODO: Move to the .gitignore file setup
                        excludeDirs = excludeDirs + rootExcludeDirs
                    }

                    project {
                        jdkName = "8.0"
                        wildcards.add("?*.gradle")
                    }
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
