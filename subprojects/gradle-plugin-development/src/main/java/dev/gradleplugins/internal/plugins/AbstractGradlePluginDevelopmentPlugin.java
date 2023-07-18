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

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentExtensionInternal;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public abstract class AbstractGradlePluginDevelopmentPlugin {
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

    public static <T> GradlePluginDevelopmentExtensionInternal registerLanguageExtension(Project project, String languageName, Class<T> type) {
        GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        GradlePluginDevelopmentExtensionInternal extension = project.getObjects().newInstance(GradlePluginDevelopmentExtensionInternal.class, project.getExtensions().getByType(JavaPluginExtension.class));
        ((ExtensionAware)gradlePlugin).getExtensions().add(type, languageName, type.cast(extension));

        project.afterEvaluate(proj -> {
            if (!extension.isDefaultRepositoriesDisabled()) {
                GradlePluginDevelopmentRepositoryExtension.from(project.getRepositories()).gradlePluginDevelopment();
            }
        });

        return extension;
    }
}
