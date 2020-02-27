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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

//@GradlePlugin(id = "dev.gradleplugins.java-gradle-plugin")
public class JavaGradlePluginDevelopmentPlugin implements Plugin<Project> { //extends AbstractGradlePluginDevelopmentPlugin {
    @Override
    public void apply(Project project) {
        // Assert java-gradle-plugin not applied
        // Configure api dependencies

        project.getPluginManager().apply("java-gradle-plugin"); // For plugin development

        project.getPluginManager().apply(GradlePluginDevelopmentFunctionalTestingPlugin.class);

//        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);

//        configureAnnotationProcessorSources(project.getTasks().named("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class));
    }

//    @Override
    protected String getPluginId() {
        return "dev.gradleplugins.java-gradle-plugin";
    }

//    private static void configureAnnotationProcessorSources(TaskProvider<FakeAnnotationProcessorTask> processorTask) {
//        processorTask.configure(task -> {
//            task.getSource().from(task.getProject().getTasks().named("compileJava", JavaCompile.class).map(JavaCompile::getSource));
//        });
//    }
}