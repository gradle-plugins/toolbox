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

package dev.gradleplugins.internal;

import com.gradle.publish.PublishPlugin;
import dev.gradleplugins.internal.tasks.FakeAnnotationProcessorTask;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import dev.gradleplugins.internal.TestFixtures;

public class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // TODO: Let's not remove the compile configuration but let's instead filter it at key location (when attached to tasks, etc) so we can remove the gradleApi()
        configureGradleApiProjectDependency(project);

        project.getPluginManager().apply(JavaGradlePluginPlugin.class); // For plugin development

        // TODO: Check if we can remove the gradlePlugin extension as there is no more usage with the annotation
        // TODO: Print deprecation warning about using GradlePlugin the container for declaring plugin IDs
        // TODO: Warn when calling gradleApi()

        project.getPluginManager().apply(SpockFunctionalTestingPlugin.class); // For functional testing
        project.getPluginManager().apply(PublishPlugin.class); // For publishing

        configureDefaultJavaCompatibility(project.getExtensions().getByType(JavaPluginExtension.class));

        // Configure the Gradle Plugin Development annotation processor
        configureGradlePluginDevelopmentAnnotationProjectDependency(project);
        TaskProvider<FakeAnnotationProcessorTask> processorTask = createAnnotationProcessorTask(project.getTasks(), project.getLayout());
        bindAnnotationProcessorOutputToSourceSetContainer(project.getExtensions().getByType(SourceSetContainer.class), processorTask);
        project.getTasks().named("jar", task -> task.dependsOn(processorTask));

        // TODO: lint java-gradle-plugin extension VS the annotation
    }

    private static void configureDefaultJavaCompatibility(JavaPluginExtension java) {
        java.setSourceCompatibility(JavaVersion.VERSION_1_8);
        java.setTargetCompatibility(JavaVersion.VERSION_1_8);
    }

    private static void configureGradlePluginDevelopmentAnnotationProjectDependency(Project project) {
        String groupId = "dev.gradleplugins";
        String artifactId = "gradle-plugin-development-annotation";
        project.getConfigurations().matching(it -> it.getName().equals("compileOnly")).all(configuration -> project.getDependencies().add(configuration.getName(), groupId + ":" + artifactId + ":" + TestFixtures.currentVersion));

        Action<? super MavenRepositoryContentDescriptor> filterContent = (MavenRepositoryContentDescriptor content) -> content.includeVersion(groupId, artifactId, TestFixtures.currentVersion);

        String repositoryName = "Gradle Plugin Development - Annotations";
        if (TestFixtures.released) {
            project.getRepositories().maven(repository -> {
                repository.setName(repositoryName);
                repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/maven"));
                repository.mavenContent(filterContent);
            });
        } else {
            project.getRepositories().mavenLocal(repository -> {
                repository.setName(repositoryName);
                repository.mavenContent(filterContent);
            });
        }
    }

    private static void configureGradleApiProjectDependency(Project project) {
        String groupId = "dev.gradleplugins";
        String artifactId = "gradle-api";
        String version = TestFixtures.apiVersion + "-5.6.2";
        project.getConfigurations().matching(it -> it.getName().equals("compileOnly")).all(configuration -> project.getDependencies().add(configuration.getName(), groupId + ":" + artifactId + ":" + version));

        Action<? super MavenRepositoryContentDescriptor> filterContent = (MavenRepositoryContentDescriptor content) -> content.includeVersion(groupId, artifactId, version);

        String repositoryName = "Gradle Plugin Development - Gradle APIs";
        if (TestFixtures.released) {
            project.getRepositories().maven(repository -> {
                repository.setName(repositoryName);
                repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/maven"));
                repository.mavenContent(filterContent);
            });
        } else {
            project.getRepositories().mavenLocal(repository -> {
                repository.setName(repositoryName);
                repository.mavenContent(filterContent);
            });
        }
    }

    private static TaskProvider<FakeAnnotationProcessorTask> createAnnotationProcessorTask(TaskContainer tasks, ProjectLayout projectLayout) {
        return tasks.register("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class, task -> {
            task.getPluginDescriptorDirectory().set(projectLayout.getBuildDirectory().dir("fake-annotation-processing"));
        });
    }

    private static void bindAnnotationProcessorOutputToSourceSetContainer(SourceSetContainer sourceSets, TaskProvider<FakeAnnotationProcessorTask> processorTask) {
        sourceSets.getByName("main").getOutput().dir(processorTask.flatMap(FakeAnnotationProcessorTask::getPluginDescriptorDirectory));
    }
}
