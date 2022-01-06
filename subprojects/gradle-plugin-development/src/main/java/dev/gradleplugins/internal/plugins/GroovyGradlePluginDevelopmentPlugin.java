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

import dev.gradleplugins.GradleRuntimeCompatibility;
import dev.gradleplugins.GroovyGradlePluginDevelopmentExtension;
import dev.gradleplugins.internal.DeferredRepositoryFactory;
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.plugins.AbstractGradlePluginDevelopmentPlugin.*;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public class GroovyGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.groovy-gradle-plugin";

    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), PLUGIN_ID);
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), PLUGIN_ID);
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), PLUGIN_ID);

        project.getPluginManager().apply("dev.gradleplugins.base");
        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
        // Starting with Gradle 6.4, precompiled Groovy DSL plugins are available
        if (GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            project.getPluginManager().apply("groovy-gradle-plugin"); // For plugin development
        } else {
            project.getPluginManager().apply("java-gradle-plugin"); // For plugin development
        }
        project.getPluginManager().apply("groovy");

        val groovy = registerLanguageExtension(project, "groovy", GroovyGradlePluginDevelopmentExtension.class);
        val extension = compatibility(gradlePlugin(project));

        // Configure the Groovy version and dependency
        groovy.getGroovyVersion().convention(extension.getMinimumGradleVersion().map(GradleRuntimeCompatibility::groovyVersionOf));
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add("compileOnly", groovy.getGroovyVersion().map(dependencies::groovy));

        // TODO: We should warn that a repository is required instead of trying to add a groovy only repository
        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);
        repositoryFactory.groovy();

        // TODO: warn if the plugin only have has Java source and no Groovy.
        //   You specified a Groovy plugin development so we should expect Gradle plugin to be all in Groovy
        //   We could ensure GradlePlugin aren't applied to any Java source
        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
        //   We could do the same for Kotlin code
    }
}