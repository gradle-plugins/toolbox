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
import org.gradle.api.tasks.compile.GroovyCompile;

@GradlePlugin(id = "dev.gradleplugins.groovy-gradle-plugin")
public class GroovyGradlePluginDevelopmentPlugin extends AbstractGradlePluginDevelopmentPlugin {
    @Override
    public void doApply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);
        project.getPluginManager().apply("groovy");

        project.getRepositories().jcenter();
        project.getDependencies().add("compileOnly", "org.codehaus.groovy:groovy-all:2.5.4"); // require jcenter()

        project.getTasks().named("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class, task -> {
            task.getSource().from(project.getTasks().named("compileGroovy", GroovyCompile.class).map(GroovyCompile::getSource));
        });

        // TODO: warn if the plugin only have has Java source and no Groovy.
        //   You specified a Groovy plugin development so we should expect Gradle plugin to be all in Groovy
        //   We could ensure GradlePlugin aren't applied to any Java source
        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
        //   We could do the same for Kotlin code
    }

    @Override
    protected String getPluginId() {
        return "dev.gradleplugins.groovy-gradle-plugin";
    }
}