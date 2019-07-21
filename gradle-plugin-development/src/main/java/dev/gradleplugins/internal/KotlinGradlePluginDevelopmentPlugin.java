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

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class KotlinGradlePluginDevelopmentPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);
        // There is no way to properly apply this plugin automatically
        // project.pluginManager.apply("org.gradle.kotlin.kotlin-dsl")
        // Instead we will crash the build if not applied manually
        project.afterEvaluate( proj -> {
            if (project.getPluginManager().findPlugin("org.gradle.kotlin.kotlin-dsl") == null) {
                throw new GradleException("You need to manually apply the `kotlin-dsl` plugin inside the plugin block:\n" +
                        "plugins {\n" +
                        "    `kotlin-dsl`\n" +
                        "}");
            }
        });

        // TODO: I think this is added by the kotlin-dsl, we will leave it out for the next versions
//        project.getDependencies().add("implementation", kotlin("gradle-plugin"));
    }
}