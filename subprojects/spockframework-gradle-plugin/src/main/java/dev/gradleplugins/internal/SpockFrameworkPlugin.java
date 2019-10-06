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
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpockFrameworkPlugin implements Plugin<Project> {
    private static final String CONVENTIONAL_TESTING_SOURCE_SET_SUFFIX = "test";
    private static final String SPOCK_FRAMEWORK_REPOSITORY_NAME = "spockFrameworkRepository";

    @Override
    public void apply(Project project) {
        // Use cases:
        //  1- JVM (need to attach to already created opinion)
        //    - java (configure test and any other sourceset)
        //    - java-library (same)
        //    - groovy (same)
        //  2- Everything else (need to create the plumbing)
        //    - cpp-application (automatic integTest)
        //    - Custom (web app) (automatic integTest)

        // TODO: probably will have to trigger on each individual plugins (java, java-library and groovy)
        project.getPluginManager().withPlugin("java-base", appliedPlugin -> {
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

            // TODO: Probably not required.. no what is requied is attaching the main sourceset output with the test source set other than `test` as it's already done
//            sourceSets.create("integTest", it -> {
//                // TODO only if java plugins
//            it.setCompileClasspath(it.getCompileClasspath().plus(sourceSets.getByName("main").getOutput()));
//            });
        });

        project.getPluginManager().withPlugin("cpp-application", appliedPlugin -> configureIntegrationTest(project));
    }

    private static void configureIntegrationTest(Project project) {
        project.getPluginManager().apply("groovy-base");

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);

        // For C++ applications, it really only make sense to be integration test of the application as a whole. We could link the object files into shared libraries and generate/export bindings to all "exported" symbols as if the Groovy code was linking with the C++ code but this is outside the scope of the current effort. Maybe in later versions.
        sourceSets.create("integTest");

        // Apply the rules, hehe, software model is still alive.
        configureTestingDependencies(project.getDependencies(), sourceSets);
        createTestingTasks(project.getTasks(), sourceSets);
        attachTestingTasksWithCheckTask(project.getTasks(), sourceSets);
        addSpockFrameworkRepository(project.getRepositories(), sourceSets);
    }

    private static void configureTestingDependencies(DependencyHandler dependencies, SourceSetContainer sourceSets) {
        sourceSets.configureEach(sourceSet -> {
            if (isTestingSourceSet(sourceSet)) {
                ModuleDependency dep = (ModuleDependency)dependencies.add(sourceSet.getName() + "Implementation", "org.spockframework:spock-core:1.2-groovy-2.5");
                // TODO: why are we excluding this
                Map exclude = new HashMap();
                exclude.put("group", "org.codehaus.groovy");
                dep.exclude(exclude);

                // TODO: Should we depend on the localGroovy or pull the remote groovy
                // The default should probably be the localGroovy() simply because it requires less network IO to make everything work.
                dependencies.add(sourceSet.getName() + "Implementation", dependencies.localGroovy());
            }
        });
    }

    private static void createTestingTasks(TaskContainer tasks, SourceSetContainer sourceSets) {
        sourceSets.configureEach(sourceSet -> {
            if (isTestingSourceSet(sourceSet)) {
                // TODO: For Java, this may already be handled... yes, only for `test`
                tasks.register(sourceSet.getName(), Test.class, it -> {
                    it.setDescription("Runs the tests for source set '" + sourceSet.getName() + "'");
                    it.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
                    it.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
                    it.setClasspath(sourceSet.getRuntimeClasspath());
                });
            }
        });
    }

    private static void attachTestingTasksWithCheckTask(TaskContainer tasks, SourceSetContainer sourceSets) {
        // TODO: java may already have this convention
        sourceSets.configureEach(sourceSet -> {
            if (isTestingSourceSet(sourceSet)) {
                tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, it -> it.dependsOn(sourceSet));
            }
        });
    }

    private static void addSpockFrameworkRepository(RepositoryHandler repositories, SourceSetContainer sourceSets) {
        sourceSets.configureEach(sourceSet -> {
            if (isTestingSourceSet(sourceSet) && repositories.findByName(SPOCK_FRAMEWORK_REPOSITORY_NAME) == null) {
                repositories.jcenter(it -> {
                    it.setName(SPOCK_FRAMEWORK_REPOSITORY_NAME);
                });
            }
        });
    }

    private static boolean isTestingSourceSet(SourceSet sourceSet) {
        return sourceSet.getName().toLowerCase(Locale.ENGLISH).endsWith(CONVENTIONAL_TESTING_SOURCE_SET_SUFFIX);
    }
}
