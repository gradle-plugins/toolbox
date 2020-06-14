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

package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentExtensionInternal;
import dev.gradleplugins.internal.GradlePluginDevelopmentRepositoryExtensionInternal;
import lombok.val;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.VersionNumber;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;

public abstract class AbstractGradlePluginDevelopmentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), getPluginId());
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), getPluginId());
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), getPluginId());
        doApply(project);
    }

    protected abstract void doApply(Project project);
    protected abstract String getPluginId();

    public static void assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(PluginManager pluginManager, String currentPluginId) {
        getOtherGradlePluginDevelopmentPlugins(currentPluginId).stream().filter(pluginManager::hasPlugin).findAny().ifPresent(id -> {
            throw new GradleException("The '" + currentPluginId + "' cannot be applied with '" + id + "', please apply just one of them.");
        });
    }

    private static List<String> getOtherGradlePluginDevelopmentPlugins(String pluginId) {
        return Stream.of("dev.gradleplugins.java-gradle-plugin", "dev.gradleplugins.groovy-gradle-plugin", "dev.gradleplugins.kotlin-gradle-plugin").filter(it -> !it.equals(pluginId)).collect(Collectors.toList());
    }

    public static void assertJavaGradlePluginIsNotPreviouslyApplied(PluginManager pluginManager, String currentPluginId) {
        // The kotlin-dsl plugin gets special treatment in Gradle and gets moved to the front of the plugin list to apply. The kotlin-dsl internally apply the java-gradle-plugin plugin as well. Because of this, we are going to ignore the specific case of the kotlin-dsl applied and have a separate assertion.
        if (pluginManager.hasPlugin("java-gradle-plugin") && !pluginManager.hasPlugin("org.gradle.kotlin.kotlin-dsl")) {
            throw new GradleException("The Gradle core plugin 'java-gradle-plugin' should not be applied within your build when using '" + currentPluginId + "'.");
        }
    }

    public static void assertKotlinDslPluginIsNeverApplied(PluginManager pluginManager, String currentPluginId) {
        pluginManager.withPlugin("org.gradle.kotlin.kotlin-dsl", appliedPlugin -> {
            throw new GradleException("The Gradle plugin 'kotlin-dsl' should not be applied within your build when using '" + currentPluginId + "'.");
        });
    }

    public static void configureDefaultJavaCompatibility(JavaPluginExtension java, VersionNumber minimumGradleVersion) {
        JavaVersion minimumJavaVersion = minimumJavaVersionFor(minimumGradleVersion);
        java.setSourceCompatibility(minimumJavaVersion);
        java.setTargetCompatibility(minimumJavaVersion);
    }

    public static GradlePluginDevelopmentCompatibilityExtension registerCompatibilityExtension(Project project) {
        GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        GradlePluginDevelopmentCompatibilityExtension extension = project.getObjects().newInstance(GradlePluginDevelopmentCompatibilityExtension.class);
        ((ExtensionAware)gradlePlugin).getExtensions().add("compatibility", extension);

        return extension;
    }

    public static <T> GradlePluginDevelopmentExtensionInternal registerLanguageExtension(Project project, String languageName, Class<T> type) {
        GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        GradlePluginDevelopmentExtensionInternal extension = project.getObjects().newInstance(GradlePluginDevelopmentExtensionInternal.class, project.getExtensions().getByType(JavaPluginExtension.class));
        ((ExtensionAware)gradlePlugin).getExtensions().add(type, languageName, type.cast(extension));

        return extension;
    }

    public static void removeGradleApiProjectDependency(Project project) {
        // Surgical procedure of removing the Gradle API and replacing it with dev.gradleplugins:gradle-api
        project.getConfigurations().getByName("api").getDependencies().removeIf(it -> {
            if (it instanceof SelfResolvingDependencyInternal) {
                return ((SelfResolvingDependencyInternal) it).getTargetComponentId().getDisplayName().equals("Gradle API");
            }
            return false;
        });
    }

    public static void configureExtension(GradlePluginDevelopmentCompatibilityExtension extension, Project project) {
        project.afterEvaluate(proj -> {
            if (extension.getMinimumGradleVersion().isPresent()) {
                configureDefaultJavaCompatibility(project.getExtensions().getByType(JavaPluginExtension.class), VersionNumber.parse(extension.getMinimumGradleVersion().get()));
            } else {
                extension.getMinimumGradleVersion().set(project.getGradle().getGradleVersion());
            }
            extension.getMinimumGradleVersion().disallowChanges();
        });
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(project, "compileOnly", extension.getMinimumGradleVersion().map(dependencies::gradleApi));
        GradlePluginDevelopmentRepositoryExtensionInternal.of(project.getRepositories()).gradlePluginDevelopment();
    }
}
