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
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.rules.JavaGradlePluginIsNotPreviouslyAppliedRule;
import dev.gradleplugins.internal.rules.KotlinDslPluginIsNeverAppliedRule;
import dev.gradleplugins.internal.rules.OtherGradlePluginDevelopmentPluginsIncompatibilityRule;
import dev.gradleplugins.internal.rules.RegisterLanguageExtensionRule;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.GradlePluginDevelopmentDependencies.dependencies;
import static dev.gradleplugins.GroovyGradlePluginDevelopmentExtension.groovy;

public class GroovyGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.groovy-gradle-plugin";

    @Override
    public void apply(Project project) {
        new OtherGradlePluginDevelopmentPluginsIncompatibilityRule(PLUGIN_ID).execute(project);
        new JavaGradlePluginIsNotPreviouslyAppliedRule(PLUGIN_ID).execute(project);
        new KotlinDslPluginIsNeverAppliedRule(PLUGIN_ID).execute(project);

        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        // Starting with Gradle 6.4, precompiled Groovy DSL plugins are available
        if (GradleVersion.current().compareTo(GradleVersion.version("6.4")) >= 0) {
            project.getPluginManager().apply("groovy-gradle-plugin"); // For plugin development
        } else {
            project.getPluginManager().apply("java-gradle-plugin"); // For plugin development
        }
        project.getPluginManager().apply("groovy");

        new RegisterLanguageExtensionRule("groovy", GroovyGradlePluginDevelopmentExtension.class).execute(project);

        // Configure the Groovy version
        gradlePlugin(project, developmentExtension -> {
            val extension = compatibility(developmentExtension);
            groovy(developmentExtension).getGroovyVersion().convention(extension.getMinimumGradleVersion().map(GradleRuntimeCompatibility::groovyVersionOf));
        });

        // Configure the Groovy dependency
        gradlePlugin(project, developmentExtension -> {
            val factory = DependencyFactory.forProject(project);
            dependencies(developmentExtension).getCompileOnly().add(groovy(developmentExtension).getGroovyVersion().map(version -> factory.create("org.codehaus.groovy:groovy-all:" + version)));
        });

        // TODO: We should warn that a repository is required instead of trying to add a groovy only repository
        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);
        repositoryFactory.groovy();

        // TODO: warn if the plugin only have has Java source and no Groovy.
        //   You specified a Groovy plugin development so we should expect Gradle plugin to be all in Groovy
        //   We could ensure GradlePlugin aren't applied to any Java source
        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
        //   We could do the same for Kotlin code
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> configureAction) {
        configureAction.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }
}