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

import dev.gradleplugins.internal.DeferredRepositoryFactory;
import dev.gradleplugins.internal.GroovyGradlePluginSpockTestSuite;
import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public class SpockFrameworkTestSuiteBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("groovy-base");
        project.getComponents().withType(GroovySpockFrameworkTestSuite.class, testSuite -> {
            SourceSet sourceSet = maybeCreateSourceSet(testSuite, project);
            createAndAttachTestTask(testSuite, sourceSet, project);

            if (testSuite.getTestedSourceSet().isPresent()) {
                sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(testSuite.getTestedSourceSet().get().getOutput()));
                sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));
            }

            testSuite.getSpockVersion().convention("1.2-groovy-2.5");
            configureSpockFrameworkProjectDependency(testSuite, sourceSet, project);
        });
    }

    private static void configureSpockFrameworkProjectDependency(GroovySpockFrameworkTestSuite testSuite, SourceSet sourceSet, Project project) {
        // TODO: Once lazy dependency is supported, see https://github.com/gradle/gradle/pull/11767
        // project.getDependencies().add(sourceSet.getImplementationConfigurationName(), testSuite.getSpockVersion().map(version -> "org.spockframework:spock-bom:" + version));
        // project.getDependencies().add(sourceSet.getImplementationConfigurationName(), "org.spockframework:spock-core");
        project.afterEvaluate(proj -> {
            project.getDependencies().add(sourceSet.getImplementationConfigurationName(), "org.codehaus.groovy:groovy-all:2.5.10"); // Use latest
            project.getDependencies().add(sourceSet.getImplementationConfigurationName(), project.getDependencies().platform("org.spockframework:spock-bom:" + testSuite.getSpockVersion().get()));
            project.getDependencies().add(sourceSet.getImplementationConfigurationName(), "org.spockframework:spock-core");
        });

        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);

        repositoryFactory.spock();
    }

    public static SourceSet maybeCreateSourceSet(GroovySpockFrameworkTestSuite testSuite, Project project) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        SourceSet sourceSet = sourceSets.findByName(testSuite.getName());
        if (sourceSet == null) {
            sourceSet = sourceSets.create(testSuite.getName(), it -> {
                GroovySourceSet groovyIt = new DslObject(it).getConvention().getPlugin(GroovySourceSet.class);
                groovyIt.getGroovy().srcDir("src/" + testSuite.getName() + "/groovy");

                it.getResources().srcDir("src/" + testSuite.getName() + "/resources");
                it.setRuntimeClasspath(it.getRuntimeClasspath().plus(it.getOutput()).plus(it.getCompileClasspath()));
            });
        }

        return sourceSet;
    }

    private TaskProvider<Test> createAndAttachTestTask(GroovySpockFrameworkTestSuite testSuite, SourceSet sourceSet, Project project) {
        TaskProvider<Test> testTask = project.getTasks().register(sourceSet.getName(), Test.class, it -> {
            it.setDescription("Runs the functional tests");
            it.setGroup("verification");

            it.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
            it.setClasspath(sourceSet.getRuntimeClasspath());
        });

        project.getTasks().named("check", it -> it.dependsOn(testTask));

        return testTask;
    }
}