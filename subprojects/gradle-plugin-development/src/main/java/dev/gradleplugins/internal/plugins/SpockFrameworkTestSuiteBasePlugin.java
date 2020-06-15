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
import dev.gradleplugins.internal.GradlePluginDevelopmentDependencyExtensionInternal;
import dev.gradleplugins.internal.GroovySpockFrameworkTestSuite;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;

import static dev.gradleplugins.internal.DefaultDependencyVersions.GROOVY_ALL_VERSION;
import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;

public class SpockFrameworkTestSuiteBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        TaskContainer tasks = project.getTasks();
        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);

        project.getPluginManager().apply("groovy-base");

        project.getComponents().withType(GroovySpockFrameworkTestSuite.class, testSuite -> {
            tasks.named("check", it -> it.dependsOn(testSuite.getTestTask()));

            project.afterEvaluate(proj -> {
                testSuite.getTestedSourceSet().disallowChanges();
                if (testSuite.getTestedSourceSet().isPresent()) {
                    SourceSet sourceSet = testSuite.getSourceSet();
                    SourceSet testedSourceSet = testSuite.getTestedSourceSet().get();
                    sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(testedSourceSet.getOutput()));
                    sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));
                }
            });

            testSuite.getSpockVersion().convention(SPOCK_FRAMEWORK_VERSION).finalizeValueOnRead();
            configureSpockFrameworkProjectDependency(testSuite.getSpockVersion(), testSuite.getSourceSet(), project);
            repositoryFactory.spock();
        });
    }

    public static void configureSpockFrameworkProjectDependency(Provider<String> spockVersion, SourceSet sourceSet, Project project) {
        val dependencies = GradlePluginDevelopmentDependencyExtensionInternal.of(project.getDependencies());
        dependencies.add(sourceSet.getImplementationConfigurationName(), dependencies.groovy(GROOVY_ALL_VERSION));
        dependencies.add(sourceSet.getImplementationConfigurationName(), spockVersion.map(dependencies::spockFrameworkPlatform));
        dependencies.add(sourceSet.getImplementationConfigurationName(), dependencies.spockFramework());
    }
}