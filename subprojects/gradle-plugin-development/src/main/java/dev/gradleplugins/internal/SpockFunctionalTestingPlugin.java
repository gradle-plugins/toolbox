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

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.MavenRepositoryContentDescriptor;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import dev.gradleplugins.internal.TestFixtures;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public class SpockFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_SOURCE_SET_NAME = "functionalTest";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("groovy-base"); // For Spock testing

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        SourceSet functionalTestSourceSet = sourceSets.create(FUNCTIONAL_TEST_SOURCE_SET_NAME, it -> {
            GroovySourceSet groovyIt = new DslObject(it).getConvention().getPlugin(GroovySourceSet.class);
            groovyIt.getGroovy().srcDir("src/functTest/groovy");
            groovyIt.getGroovy().srcDir("src/functionalTest/groovy");

            it.getResources().srcDir("src/functTest/resources");
            it.getResources().srcDir("src/functionalTest/resources");
            it.setCompileClasspath(it.getCompileClasspath().plus(sourceSets.getByName("main").getOutput()));
            it.setRuntimeClasspath(it.getRuntimeClasspath().plus(it.getOutput()).plus(it.getCompileClasspath()));
        });

        configureSpockFrameworkProjectDependency(project);
        configureTestKitProjectDependency(project);
        configureGradleTestKitFixturesProjectDependency(project);

        TaskProvider<Test> functionalTest = project.getTasks().register(FUNCTIONAL_TEST_SOURCE_SET_NAME, Test.class, it -> {
            it.setDescription("Runs the functional tests");
            it.setGroup("verification");
            it.setTestClassesDirs(functionalTestSourceSet.getOutput().getClassesDirs());
            it.setClasspath(functionalTestSourceSet.getRuntimeClasspath());
            it.mustRunAfter("test");
        });

        project.getTasks().named("check", it -> it.dependsOn(functionalTest));

        // Configure functionalTest for GradlePluginDevelopmentExtension
        GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        gradlePlugin.testSourceSets(project.getExtensions().getByType(SourceSetContainer.class).getByName(FUNCTIONAL_TEST_SOURCE_SET_NAME));
    }

    private static void configureTestKitProjectDependency(Project project) {
        project.getConfigurations().matching(it -> it.getName().equals(FUNCTIONAL_TEST_SOURCE_SET_NAME + "Implementation")).configureEach( it -> {
            project.getDependencies().add(it.getName(), project.getDependencies().gradleTestKit());
        });
    }

    private static void configureSpockFrameworkProjectDependency(Project project) {
        String groupId = "org.spockframework";
        String artifactId = "spock-core";
        String version = "1.2-groovy-2.5";

        Configuration configuration = project.getConfigurations().create(FUNCTIONAL_TEST_SOURCE_SET_NAME + "SpockFrameworkImplementation");
        project.getConfigurations().matching(it -> it.getName().equals(FUNCTIONAL_TEST_SOURCE_SET_NAME + "Implementation")).configureEach(it -> {
            it.extendsFrom(configuration);
        });

        project.getDependencies().add(configuration.getName(), groupId + ":" + artifactId + ":" + version);

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

    private static void configureGradleTestKitFixturesProjectDependency(Project project) {
        String groupId = "dev.gradleplugins";
        String artifactId = "gradle-testkit-fixtures";
        String version = TestFixtures.currentVersion;

        Configuration configuration = project.getConfigurations().create(FUNCTIONAL_TEST_SOURCE_SET_NAME + "FixtureImplementation");
        project.getConfigurations().matching(it -> it.getName().equals(FUNCTIONAL_TEST_SOURCE_SET_NAME + "Implementation")).configureEach(it -> {
            it.extendsFrom(configuration);
        });

        project.getDependencies().add(configuration.getName(), groupId + ":" + artifactId + ":" + version);

        String repositoryName = "Gradle Plugins Development - TestKit Fixtures";
        Action<? super MavenRepositoryContentDescriptor> filterContent = (MavenRepositoryContentDescriptor content) -> content.includeVersion(groupId, artifactId, version);
        if (TestFixtures.released) {
            project.getRepositories().maven(repository -> {
                repository.setName(repositoryName);
                repository.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/maven"));
                repository.mavenContent(filterContent);
            });
        } else {
            configuration.getResolutionStrategy().cacheChangingModulesFor(0, "seconds");
            project.getRepositories().mavenLocal(repository -> {
                repository.setName(repositoryName);
                repository.mavenContent(filterContent);
            });
        }
    }
}