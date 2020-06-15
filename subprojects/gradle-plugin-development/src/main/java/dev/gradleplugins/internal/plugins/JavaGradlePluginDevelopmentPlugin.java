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
import dev.gradleplugins.JavaGradlePluginDevelopmentExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.plugins.AbstractGradlePluginDevelopmentPlugin.*;

public class JavaGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.java-gradle-plugin";
    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), PLUGIN_ID);
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), PLUGIN_ID);
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), PLUGIN_ID);

        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
        project.getPluginManager().apply("java-gradle-plugin"); // For plugin development
        removeGradleApiProjectDependency(project);

        registerLanguageExtension(project, "java", JavaGradlePluginDevelopmentExtension.class);
        registerCompatibilityExtension(project);

        project.getPluginManager().apply(GradlePluginDevelopmentFunctionalTestingPlugin.class);
    }
}