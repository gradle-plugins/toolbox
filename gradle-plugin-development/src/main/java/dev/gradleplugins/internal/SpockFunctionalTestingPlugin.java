/*
 * Copyright ${"$"}{today.year} the original author or authors.
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
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.tasks.GroovySourceSet;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;

import java.util.HashMap;
import java.util.Map;

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

        ModuleDependency dep = (ModuleDependency)project.getDependencies().add("functionalTestImplementation", "org.spockframework:spock-core:1.2-groovy-2.5");
        Map exclude = new HashMap();
        exclude.put("group", "org.codehaus.groovy");
        dep.exclude(exclude);
        project.getDependencies().add("functionalTestImplementation", project.getDependencies().gradleTestKit());
        project.getDependencies().add("functionalTestFixtureImplementation", TestFixtures.notation);

        if (TestFixtures.released) {
            project.getRepositories().maven(it -> {
                it.setName("Gradle Plugins Release");
                it.setUrl(project.uri("https://dl.bintray.com/gradle-plugins/maven"));
            });
        } else {
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