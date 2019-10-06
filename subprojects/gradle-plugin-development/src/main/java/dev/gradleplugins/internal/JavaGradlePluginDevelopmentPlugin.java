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

import dev.gradleplugins.GradlePlugin;
import dev.gradleplugins.internal.tasks.FakeAnnotationProcessorTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

@GradlePlugin(id = "dev.gradleplugins.java-gradle-plugin")
public class JavaGradlePluginDevelopmentPlugin extends AbstractGradlePluginDevelopmentPlugin {
    @Override
    public void doApply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);

        configureAnnotationProcessorSources(project.getTasks().named("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class));
    }

    @Override
    protected String getPluginId() {
        return "dev.gradleplugins.java-gradle-plugin";
    }

    private static void configureAnnotationProcessorSources(TaskProvider<FakeAnnotationProcessorTask> processorTask) {
        processorTask.configure(task -> {
            task.getSource().from(task.getProject().getTasks().named("compileJava", JavaCompile.class).map(JavaCompile::getSource));
        });
    }
}