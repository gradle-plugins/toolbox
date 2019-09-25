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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGradlePluginDevelopmentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        getOtherGradlePluginDevelopmentPlugins().stream().filter(id -> project.getPluginManager().hasPlugin(id)).findAny().ifPresent(id -> {
            // TODO: Maybe turn this into an exception
            project.getLogger().warn("The '" + getPluginId() + "' cannot be applied with '" + id + "', please apply just one of them.");
        });
        if (project.getPluginManager().hasPlugin("java-gradle-plugin")) {
            // TODO: Maybe turn this into an exception
            project.getLogger().warn("The Gradle core plugin 'java-gradle-plugin' should not be applied within your build when using '" + getPluginId() + "'.");
        }
        doApply(project);
    }

    private Set<String> getOtherGradlePluginDevelopmentPlugins() {
        return Stream.of("dev.gradleplugins.java-gradle-plugin", "dev.gradleplugins.groovy-gradle-plugin", "dev.gradleplugins.kotlin-gradle-plugin").filter(it -> !it.equals(getPluginId())).collect(Collectors.toSet());
    }

    protected abstract void doApply(Project project);
    protected abstract String getPluginId(); // TODO: Maybe use GradlePlugin annotation
}
