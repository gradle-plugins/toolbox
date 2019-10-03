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
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import dev.gradleplugins.internal.TestFixtures;

public class SpockFunctionalTestingPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("groovy-base"); // For Spock testing
        configureFunctionalTestingWithSpockAndTestKit(project);
    }

    private void configureFunctionalTestingWithSpockAndTestKit(Project project) {
        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        SourceSet functionalTestSourceSet = sourceSets.create("functionalTest", it -> {

            GroovySourceSet groovyIt = new DslObject(it).getConvention().getPlugin(GroovySourceSet.class);
            groovyIt.getGroovy().srcDir("src/functTest/groovy");
            groovyIt.getGroovy().srcDir("src/functionalTest/groovy");

            it.getResources().srcDir("src/functTest/resources");
            it.getResources().srcDir("src/functionalTest/resources");
            it.setCompileClasspath(it.getCompileClasspath().plus(sourceSets.getByName("main").getOutput()));
            it.setRuntimeClasspath(it.getRuntimeClasspath().plus(it.getOutput()).plus(it.getCompileClasspath()));
        });

        Configuration functionalTestFixtureConfiguration = project.getConfigurations().create("functionalTestFixtureImplementation");
        project.getConfigurations().named("functionalTestImplementation", it -> {
            it.extendsFrom(functionalTestFixtureConfiguration);
        });

        project.getDependencies().add("functionalTestImplementation", "org.spockframework:spock-core:1.2-groovy-2.5");
        project.getDependencies().add("functionalTestImplementation", project.getDependencies().gradleTestKit());
        project.getDependencies().add("functionalTestFixtureImplementation", TestFixtures.notation);

        // TODO: We should lock this repo content for only Spock and it's dependencies that we are resolving here (for version 1.2-groovy-2.5)
        project.getRepositories().jcenter(); // for spock-core

        if (TestFixtures.released) {
            // TODO: We should lock this repo content for only our fixture (we don't use any dependencies)
            project.getRepositories().maven(it -> {
                it.setName("Gradle Plugins Release");
                it.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/maven"));
            });
        } else {
            // TODO: remove as bintray doesn't allow SNAPSHOT publication OR find a workaround
            project.getRepositories().maven(it -> {
                it.setName("Gradle Plugins Snapshot");
                it.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/maven-snapshot"));
            });
            functionalTestFixtureConfiguration.getResolutionStrategy().cacheChangingModulesFor(0, "seconds");
            project.getRepositories().mavenLocal();
        }

        TaskProvider<Test> functionalTest = project.getTasks().register("functionalTest", Test.class, it -> {
            it.setDescription("Runs the functional tests");
            it.setGroup("verification");
            it.setTestClassesDirs(functionalTestSourceSet.getOutput().getClassesDirs());
            it.setClasspath(functionalTestSourceSet.getRuntimeClasspath());
            it.mustRunAfter("test");
        });

        project.getTasks().named("check", it -> it.dependsOn(functionalTest));
    }
}