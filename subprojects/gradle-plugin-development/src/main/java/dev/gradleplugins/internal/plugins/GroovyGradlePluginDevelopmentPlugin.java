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

import dev.gradleplugins.GroovyGradlePluginDevelopmentExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.util.VersionNumber;

import static dev.gradleplugins.internal.plugins.AbstractGradlePluginDevelopmentPlugin.*;

public class GroovyGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.groovy-gradle-plugin";
    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), PLUGIN_ID);
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), PLUGIN_ID);
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), PLUGIN_ID);

//        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);
        project.getPluginManager().apply("java-gradle-plugin"); // For plugin development
        project.getPluginManager().apply("groovy-base");

        GroovyGradlePluginDevelopmentExtension extension = registerExtraExtension(project, GroovyGradlePluginDevelopmentExtension.class);

        project.afterEvaluate(proj -> {
            if (extension.getMinimumGradleVersion().isPresent()) {
                configureDefaultJavaCompatibility(project.getExtensions().getByType(JavaPluginExtension.class), VersionNumber.parse(extension.getMinimumGradleVersion().get()));
            } else {
                extension.getMinimumGradleVersion().set(project.getGradle().getGradleVersion());
            }
            extension.getMinimumGradleVersion().disallowChanges();
        });

        project.getPluginManager().apply(GradlePluginDevelopmentFunctionalTestingPlugin.class);

        // Using version 2.5.2
        project.getDependencies().add("compileOnly", "org.codehaus.groovy:groovy:2.5.2");
        project.getRepositories().mavenCentral(repo -> {
            repo.mavenContent(content -> {
                content.includeVersion("org.codehaus.groovy", "groovy", "2.5.2");
            });
        });

//        configureAnnotationProcessorSources(project.getTasks().named("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class));

        // TODO: warn if the plugin only have has Java source and no Groovy.
        //   You specified a Groovy plugin development so we should expect Gradle plugin to be all in Groovy
        //   We could ensure GradlePlugin aren't applied to any Java source
        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
        //   We could do the same for Kotlin code
    }

//    @Override
//    protected String getPluginId() {
//        return "dev.gradleplugins.groovy-gradle-plugin";
//    }

//    private static void configureAnnotationProcessorSources(TaskProvider<FakeAnnotationProcessorTask> processorTask) {
//        processorTask.configure(task -> {
//            task.getSource().from(task.getProject().getTasks().named("compileGroovy", GroovyCompile.class).map(GroovyCompile::getSource));
//        });
//    }
}