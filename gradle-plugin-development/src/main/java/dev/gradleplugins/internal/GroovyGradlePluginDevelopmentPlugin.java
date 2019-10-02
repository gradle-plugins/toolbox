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
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;

@GradlePlugin(id = "dev.gradleplugins.groovy-gradle-plugin")
public class GroovyGradlePluginDevelopmentPlugin extends AbstractGradlePluginDevelopmentPlugin {
    @Override
    public void doApply(Project project) {
        project.getPluginManager().apply("groovy");
        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);

        project.getRepositories().jcenter();
        project.getDependencies().add("implementation", "org.codehaus.groovy:groovy-all:2.5.4"); // require jcenter()

        TaskProvider<FakeAnnotationProcessorTask> fakeAnnotationProcessorTask = project.getTasks().register("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class, task -> {
            task.getPluginDescriptorDirectory().set(project.getLayout().getBuildDirectory().dir("pluginDescriptors"));
        });

        project.getExtensions().getByType(SourceSetContainer.class).getByName("main").getResources().srcDir(fakeAnnotationProcessorTask.flatMap(FakeAnnotationProcessorTask::getPluginDescriptorDirectory));

        // TODO: lint java-gradle-plugin extension VS the annotation

        project.getTasks().named("pluginDescriptors", it -> {
            it.dependsOn(fakeAnnotationProcessorTask);
            it.setEnabled(false);
        });
    }

    @Override
    protected String getPluginId() {
        return "dev.gradleplugins.groovy-gradle-plugin";
    }
}