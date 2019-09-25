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
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import dev.gradleplugins.internal.TestFixtures;

import java.io.File;

public class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaGradlePluginPlugin.class); // For plugin development

        // TODO: Check if we can remove the gradlePlugin extension as there is no more usage with the annotation
        // TODO: Print deprecation warning about using GradlePlugin the container for declaring plugin IDs

        project.getConfigurations().removeIf(it -> it.getName().equals("compile"));

        if (!TestFixtures.released) {
            project.getRepositories().mavenLocal();
        }
        project.getDependencies().add("implementation", "dev.gradleplugins:gradle-api:" + TestFixtures.apiVersion + "-5.6.2");

        project.getPluginManager().apply(SpockFunctionalTestingPlugin.class);
        project.getPluginManager().apply(PublishPlugin.class); // For publishing

        // Configure annotation processor
        project.getDependencies().add("annotationProcessor", "dev.gradleplugins:gradle-plugin-development-processor:" + TestFixtures.currentVersion);
        project.getDependencies().add("implementation", "dev.gradleplugins:gradle-plugin-development-annotation:" + TestFixtures.currentVersion);

        // Force Java 8 version
        JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
        java.setSourceCompatibility(JavaVersion.VERSION_1_8);
        java.setTargetCompatibility(JavaVersion.VERSION_1_8);

        GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        gradlePlugin.testSourceSets(project.getExtensions().getByType(SourceSetContainer.class).getByName("functionalTest"));
    }
}
