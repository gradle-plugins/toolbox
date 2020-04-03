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

            configureSpockFrameworkProjectDependency(testSuite, sourceSet, project);
        });

        project.getComponents().withType(GroovyGradlePluginSpockTestSuite.class, testSuite -> {
            SourceSet sourceSet = maybeCreateSourceSet(testSuite, project);

            // This is synonym of testedComponent. For GradlePlugin spock test, we automatically depends on main source set
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(sourceSets.getByName("main").getOutput()));
            sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));


            // Configure functionalTest for GradlePluginDevelopmentExtension
            GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
            gradlePlugin.testSourceSets(sourceSet);

            configureTestKitProjectDependency(testSuite, sourceSet, project);
            configureGradleFixturesProjectDependency(testSuite, sourceSet, project);
        });
    }

    private static void configureTestKitProjectDependency(GroovyGradlePluginSpockTestSuite testSuite, SourceSet sourceSet, Project project) {
        project.getConfigurations().matching(it -> it.getName().equals(sourceSet.getImplementationConfigurationName())).configureEach( it -> {
            project.getDependencies().add(it.getName(), project.getDependencies().gradleTestKit());
        });
    }

    private static void configureSpockFrameworkProjectDependency(GroovySpockFrameworkTestSuite testSuite, SourceSet sourceSet, Project project) {
        String groupId = "org.spockframework";
        String artifactId = "spock-core";
        String version = "1.2-groovy-2.5";

        project.getConfigurations().matching(it -> it.getName().equals(sourceSet.getImplementationConfigurationName())).configureEach(it -> {
        });

        project.getDependencies().add(sourceSet.getImplementationConfigurationName(), groupId + ":" + artifactId + ":" + version);

        project.getRepositories().mavenCentral(repository -> {
            repository.setName("Gradle Plugin Development - Spock Framework");
            repository.mavenContent(content -> {
                content.includeVersion("org.spockframework", "spock-core", "1.2-groovy-2.5");
                content.includeVersion("junit", "junit", "4.12");
                content.includeVersion("org.hamcrest", "hamcrest-core", "1.3");

                // For groovy:2.5.2
                content.includeVersionByRegex("org.codehaus.groovy", "groovy.*", "2\\.5\\.2");
            });
        });
    }

    private static void configureGradleFixturesProjectDependency(GroovyGradlePluginSpockTestSuite testSuite, SourceSet sourceSet, Project project) {
        configureProjectDependency(project, "Gradle Fixtures", sourceSet.getImplementationConfigurationName(), "dev.gradleplugins", "gradle-fixtures", "0.0.29"); // lag behind one version so the test works ;-)
    }

    private static void configureProjectDependency(Project project, String repositoryDisplayName, String configurationName, String groupId, String artifactId, String version) {
        project.getConfigurations().matching(it -> it.getName().equals(configurationName)).configureEach(it -> {
            ModuleDependency dep = (ModuleDependency) project.getDependencies().add(it.getName(), groupId + ":" + artifactId + ":" + version);
            dep.capabilities(h -> h.requireCapability("dev.gradleplugins:gradle-fixtures-spock-support"));
        });

        project.getRepositories().maven(repository -> {
            repository.setName("Gradle Plugins Development - " + repositoryDisplayName);
            repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/distributions"));
            repository.mavenContent(content -> content.includeVersion(groupId, artifactId, version));
        });
    }

    private SourceSet maybeCreateSourceSet(GroovySpockFrameworkTestSuite testSuite, Project project) {
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