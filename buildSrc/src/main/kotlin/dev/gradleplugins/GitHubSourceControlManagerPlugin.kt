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

import com.gradle.publish.PluginBundleExtension
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.closureOf
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

open class GitHubSourceControlManagerPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val gitHub = project.extensions.create("gitHub", GitHubSourceControlManagerExtension::class.java)
        gitHub.gitHubHostName.convention("github.com")
        gitHub.gitHubRepositoryName.convention(rootProject.name)

        pluginManager.withPlugin("maven-publish") {
            configure<PublishingExtension> {
                publications.withType(MavenPublication::class.java) {
                    pom {
                        description.set(project.provider { project.description })
                        url.set(gitHub.gitHubWebsiteUrl.map { it.toString() })

                        scm {
                            url.set(gitHub.gitHubWebsiteUrl.map { it.toString() })
                            connection.set(gitHub.sourceControlManagerUrl(GitHubSourceControlManagerExtension.SourceControlManagerProtocol.HTTPS).map { "scm:$it" })
                            developerConnection.set(gitHub.sourceControlManagerUrl(GitHubSourceControlManagerExtension.SourceControlManagerProtocol.GIT).map { "scm:$it" })
                        }

                        issueManagement {
                            system.set("GitHub")
                            url.set(gitHub.gitHubIssueTrackerUrl.map { it.toString() })
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
                        userOrg = gitHub.gitHubOrganization.get()
                        websiteUrl = gitHub.gitHubWebsiteUrl.get().toString()
                        issueTrackerUrl = gitHub.gitHubIssueTrackerUrl.get().toString()
                        vcsUrl = gitHub.sourceControlManagerUrl(GitHubSourceControlManagerExtension.SourceControlManagerProtocol.HTTPS).get().toString()

                        githubRepo = gitHub.gitHubRepositorySlug.get()
                    })
                }
            }
        }

        pluginManager.withPlugin("org.jetbrains.gradle.plugin.idea-ext") {
            configure<IdeaModel> {
                project {
                    vcs = "Git"
                }
            }
        }

        pluginManager.withPlugin("com.gradle.plugin-publish") {
            afterEvaluate {
                configure<PluginBundleExtension> {
                    vcsUrl = gitHub.gitHubWebsiteUrl.get().toString()
                }
            }
        }

        // TODO: Allow to configure the repository description/topic/website
    }
}