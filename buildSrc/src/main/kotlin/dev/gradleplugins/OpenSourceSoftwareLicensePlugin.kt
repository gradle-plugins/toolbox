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
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.gradle.ext.ProjectSettings
import java.net.URL

class OpenSourceSoftwareLicensePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val ossLicense = project.extensions.create("ossLicense", OpenSourceSoftwareLicenseExtension::class.java)

        pluginManager.withPlugin("maven-publish") {
            configure<PublishingExtension> {
                publications.withType(MavenPublication::class.java) {
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

        pluginManager.withPlugin("com.jfrog.bintray") {
            // TODO: Remove once bintray support Provider API
            afterEvaluate {
                configure<BintrayExtension> {
                    pkg(closureOf<BintrayExtension.PackageConfig> {
                        setLicenses(ossLicense.name.get())
                    })
                }
            }
        }

        pluginManager.withPlugin("org.jetbrains.gradle.plugin.idea-ext") {
            plugins.withType<IdeaPlugin> {
                with(model) {
                    project {
                        settings {
                            configureCopyright(ossLicense)
                        }
                    }
                }
            }
        }

        plugins.withType<SetupProjectPlugin> {
            // TODO: Only in rootProject
            if (isRootProject()) {
                val generateLicenseFileTask = tasks.register("generateLicenseFile") {
                    doLast {
                        file("LICENSE").writeText(ossLicense.licenseUrl.get().readText())
                    }
                }

                tasks.named("setup") {
                    dependsOn(generateLicenseFileTask)
                }
            }
        }
    }
}

private fun Project.isRootProject(): Boolean {
    return project.parent == null
}

private
fun ProjectSettings.configureCopyright(ossLicense: OpenSourceSoftwareLicenseExtension) {
    copyright {
        useDefault = ossLicense.shortName.get()
        profiles {
            create(ossLicense.shortName.get()) {
                notice = ossLicense.copyrightFileHeader.get()
                keyword = "Copyright"
            }
        }
    }
}