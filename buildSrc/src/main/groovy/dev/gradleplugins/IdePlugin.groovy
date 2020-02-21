package dev.gradleplugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.idea.IdeaPlugin
import java.io.File

class IdePlugin implements Plugin<Project> {

    void apply(Project project) {
        configureIdeaForRootProject(project)
    }

    private void configureIdeaForRootProject(Project project) {
        project.pluginManager.withPlugin("org.jetbrains.gradle.plugin.idea-ext") {
            project.tasks.named("idea") {
                doFirst {
                    throw new RuntimeException("To import in IntelliJ, please follow the instructions here: https://github.com/gradle/gradle/blob/master/CONTRIBUTING.md#intellij but use this repo ;-)")
                }
            }

            // TODO: Prompt on `idea` plugin id, the type is outside of our boundary
            project.plugins.withType(IdeaPlugin) {
                model.with {
                    module {
                        // TODO: Move to the .gitignore file setup
                        excludeDirs = excludeDirs + rootExcludeDirs(project)
                    }

                    project {
                        jdkName = "8.0"
                        wildcards.add("?*.gradle")
                    }
                }
            }
        }
    }

    private Set<File> rootExcludeDirs(Project project) {
        return [project.file("intTestHomeDir"),
                project.file("buildSrc/build"),
                project.file("buildSrc/.gradle")]
    }
}