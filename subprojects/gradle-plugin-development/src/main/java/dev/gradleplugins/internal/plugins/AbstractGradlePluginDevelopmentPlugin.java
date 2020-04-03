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

import org.gradle.api.*;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.Provider;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;
import org.gradle.util.VersionNumber;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGradlePluginDevelopmentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), getPluginId());
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), getPluginId());
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), getPluginId());
        doApply(project);
    }

    protected abstract void doApply(Project project);
    protected abstract String getPluginId(); // TODO: Maybe use GradlePlugin annotation

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

    private static JavaVersion toMinimumJavaVersion(VersionNumber version) {
        switch (version.getMajor()) {
            case 0:
                throw new UnsupportedOperationException("I didn't have time to figure out what is the minimum Java version for Gradle version below 1.0. Feel free to open an issue and look into that for me.");
            case 1:
                return JavaVersion.VERSION_1_5;
            case 2:
                return JavaVersion.VERSION_1_6;
            case 3:
            case 4:
                return JavaVersion.VERSION_1_7;
            case 5:
            case 6:
                return JavaVersion.VERSION_1_8;
            default:
                throw new IllegalArgumentException("Version not known at the time, please check what Java version is supported");
        }
    }

    public static void configureDefaultJavaCompatibility(JavaPluginExtension java, VersionNumber minimumGradleVersion) {
        JavaVersion minimumJavaVersion = toMinimumJavaVersion(minimumGradleVersion);
        java.setSourceCompatibility(minimumJavaVersion);
        java.setTargetCompatibility(minimumJavaVersion);
    }

    public static <T> T registerExtraExtension(Project project, Class<T> type) {
        GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        T extension = project.getObjects().newInstance(type);
        ((ExtensionAware)gradlePlugin).getExtensions().add("extra", extension);

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

    public static void configureGradleApiDependencies(Project project, Provider<String> minimumGradleVersion) {
        // TODO: Once lazy dependency is supported, see https://github.com/gradle/gradle/pull/11767
        // project.getDependencies().add("compileOnly", minimumGradleVersion.map(version -> "dev.gradleplugins:gradle-api:" + version));
        project.afterEvaluate(proj -> {
            project.getDependencies().add("compileOnly", "dev.gradleplugins:gradle-api:" + minimumGradleVersion.get());
        });

        // Gives a chance to the user to insert another repository before this one
        project.afterEvaluate(proj -> {
            project.getRepositories().maven(repository -> {
                repository.setName("Gradle Plugin Development - Gradle APIs");
                repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/distributions"));
                repository.mavenContent(content -> {
                    content.includeModule("dev.gradleplugins", "gradle-api");
                });
            });
        });
    }
}
