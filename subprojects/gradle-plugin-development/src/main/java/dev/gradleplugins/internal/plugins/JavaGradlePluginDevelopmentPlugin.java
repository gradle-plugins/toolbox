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

import dev.gradleplugins.JavaGradlePluginDevelopmentExtension;
import dev.gradleplugins.internal.rules.JavaGradlePluginIsNotPreviouslyAppliedRule;
import dev.gradleplugins.internal.rules.KotlinDslPluginIsNeverAppliedRule;
import dev.gradleplugins.internal.rules.OtherGradlePluginDevelopmentPluginsIncompatibilityRule;
import dev.gradleplugins.internal.rules.RegisterLanguageExtensionRule;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JavaGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.java-gradle-plugin";

    @Override
    public void apply(Project project) {
        new OtherGradlePluginDevelopmentPluginsIncompatibilityRule(PLUGIN_ID).execute(project);
        new JavaGradlePluginIsNotPreviouslyAppliedRule(PLUGIN_ID).execute(project);
        new KotlinDslPluginIsNeverAppliedRule(PLUGIN_ID).execute(project);

        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        project.getPluginManager().apply("java-gradle-plugin"); // For plugin development

        new RegisterLanguageExtensionRule("java", JavaGradlePluginDevelopmentExtension.class).execute(project);
    }
}