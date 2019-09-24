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

package dev.gradleplugins.scm.internal;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.plugins.ide.idea.model.IdeaModel;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

public class GitHubSourceControlManagerPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        GitHubSourceControlManagerExtension gitHub = project.getExtensions().create("gitHub", GitHubSourceControlManagerExtension.class);
        gitHub.getGitHubHostName().convention("github.com");
        gitHub.getGitHubRepositoryName().convention(project.getRootProject().getName());

        project.getPluginManager().withPlugin("maven-publish", appliedPlugin -> {
            PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
            publishing.getPublications().withType(MavenPublication.class, publication -> {
                publication.pom(pom -> {
                    pom.getDescription().set(project.provider(project::getDescription));
                    pom.getUrl().set(gitHub.getGitHubWebsiteUrl().map(URL::toString));
                    pom.scm(scm -> {
                        scm.getUrl().set(gitHub.getGitHubWebsiteUrl().map(URL::toString));
                        scm.getConnection().set(gitHub.sourceControlManagerUrl(GitHubSourceControlManagerExtension.SourceControlManagerProtocol.HTTPS).map(it -> "scm:" + it));
                        scm.getDeveloperConnection().set(gitHub.sourceControlManagerUrl(GitHubSourceControlManagerExtension.SourceControlManagerProtocol.GIT).map(it -> "scm:" + it));
                    });

                    pom.issueManagement(issueManagement -> {
                        issueManagement.getSystem().set("GitHub");
                        issueManagement.getUrl().set(gitHub.getGitHubIssueTrackerUrl().map(URL::toString));
                    });
                });
            });
        });

        project.getPluginManager().withPlugin("com.jfrog.bintray", appliedPlugin -> {
            // TODO: Remove once bintray support Provider API
            project.afterEvaluate(evaluatedProject -> {
                try {
                    Class extensionClass = Class.forName("com.jfrog.bintray.gradle.BintrayExtension");
                    Object extension = evaluatedProject.getExtensions().getByType(extensionClass);
                    Object pkg = get(extension, "getPkg");

                    set(pkg, "setUserOrg", gitHub.getGitHubOrganization().get());
                    set(pkg, "setWebsiteUrl", gitHub.getGitHubWebsiteUrl().get().toString());
                    set(pkg, "setIssueTrackerUrl", gitHub.getGitHubIssueTrackerUrl().get().toString());
                    set(pkg, "setVcsUrl", gitHub.sourceControlManagerUrl(GitHubSourceControlManagerExtension.SourceControlManagerProtocol.HTTPS).get().toString());

                    Object version = get(pkg, "getVersion");
                    set(version, "setVcsTag", "v" + project.getVersion().toString());

                    set(pkg, "setGithubRepo", gitHub.getGitHubRepositorySlug().get());
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        });

        project.getPluginManager().withPlugin("org.jetbrains.gradle.plugin.idea-ext", appliedPlugin -> {
            IdeaModel extension = project.getExtensions().getByType(IdeaModel.class);
            extension.getProject().setVcs("Git");
        });

        project.getPluginManager().withPlugin("com.gradle.plugin-publish", appliedPlugin -> {
            project.afterEvaluate(evaluatedProject -> {
                try {
                    Class extensionClass = Class.forName("ccom.gradle.publish.PluginBundleExtension");
                    Object extension = evaluatedProject.getExtensions().getByType(extensionClass);
                    set(extension, "setVcsUrl", gitHub.getGitHubWebsiteUrl().get().toString());
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            });
        });

        // TODO: Allow to configure the repository description/topic/website
    }

    private static Object get(Object instance, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return instance.getClass().getMethod(methodName).invoke(instance);
    }

    private static Object set(Object instance, String methodName, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return instance.getClass().getMethod(methodName, value.getClass()).invoke(instance, value);
    }
}
