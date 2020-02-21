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

import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.gradle.ext.ProjectSettings
import java.net.URL

class OpenSourceSoftwareLicensePlugin implements Plugin<Project> {
    void apply(Project project) {
        def ossLicense = project.extensions.create("ossLicense", OpenSourceSoftwareLicenseExtension)

        project.pluginManager.withPlugin("maven-publish") {
            project.publishing {
                publications.withType(MavenPublication) {
                    pom {
                        licenses {
                            license {
                                name.set(ossLicense.displayName)
                                url.set(ossLicense.licenseUrl.map { it.toString() })
                                distribution.set("repo")
                            }
                        }
                    }
                }
            }
        }

        project.pluginManager.withPlugin("com.jfrog.bintray") {
            // TODO: Remove once bintray support Provider API
            project.afterEvaluate {
                project.extensions.configure(BintrayExtension) { bintray ->
                    bintray.pkg {
                        setLicenses(ossLicense.name.get())
                    }
                }
            }
        }

        project.pluginManager.withPlugin("org.jetbrains.gradle.plugin.idea-ext") {
            project.plugins.withType(IdeaPlugin) {
                model.with {
                    project {
                        settings {
                            configureCopyright(ossLicense)
                        }
                    }
                }
            }
        }

        project.plugins.withType(SetupProjectPlugin) {
            // TODO: Only in rootProject
            if (isRootProject(project)) {
                def generateLicenseFileTask = project.tasks.register("generateLicenseFile") {
                    doLast {
                        file("LICENSE").writeText(ossLicense.licenseUrl.get().readText())
                    }
                }

                project.tasks.named("setup") {
                    dependsOn(generateLicenseFileTask)
                }
            }
        }
    }

    private boolean isRootProject(Project project) {
        return project.parent == null;
    }

    private void configureCopyright(ProjectSettings projectSettings, OpenSourceSoftwareLicenseExtension ossLicense) {
        projectSettings.copyright {
            useDefault = ossLicense.shortName.get()
            profiles {
                create(ossLicense.shortName.get()) {
                    notice = ossLicense.copyrightFileHeader.get()
                    keyword = "Copyright"
                }
            }
        }
    }
}
