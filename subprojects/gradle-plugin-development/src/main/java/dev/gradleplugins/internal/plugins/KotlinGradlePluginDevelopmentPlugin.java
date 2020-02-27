///*
// * Copyright 2019 the original author or authors.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      https://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package dev.gradleplugins.internal;
//
//import dev.gradleplugins.GradlePlugin;
//import dev.gradleplugins.internal.tasks.FakeAnnotationProcessorTask;
//import org.gradle.api.GradleException;
//import org.gradle.api.Project;
//import org.gradle.api.tasks.SourceTask;
//import org.gradle.api.tasks.TaskProvider;
//import org.gradle.api.tasks.compile.AbstractCompile;
//
//@GradlePlugin(id = "dev.gradleplugins.kotlin-gradle-plugin")
//public class KotlinGradlePluginDevelopmentPlugin extends AbstractGradlePluginDevelopmentPlugin {
//    @Override
//    public void doApply(Project project) {
//        project.getPluginManager().apply(GradlePluginDevelopmentBasePlugin.class);
//        project.afterEvaluate(KotlinGradlePluginDevelopmentPlugin::assertKotlinPluginWasApplied);
//
//        project.getPluginManager().withPlugin("org.jetbrains.kotlin.jvm", appliedPlugin -> configureAnnotationProcessorSources(project.getTasks().named("fakeAnnotationProcessing", FakeAnnotationProcessorTask.class)));
//
//        // TODO: warn if the plugin only have has Java source and no Kotlin.
//        //   You specified a Kotlin plugin development so we should expect Gradle plugin to be all in Kotlin
//        //   We could ensure GradlePlugin aren't applied to any Java source
//        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
//        //   We could do the same for Groovy code
//    }
//
//    @Override
//    protected String getPluginId() {
//        return "dev.gradleplugins.kotlin-gradle-plugin";
//    }
//
//    private static void configureAnnotationProcessorSources(TaskProvider<FakeAnnotationProcessorTask> processorTask) {
//        processorTask.configure(task -> {
//            task.getSource().from(task.getProject().getTasks().named("compileKotlin", AbstractCompile.class).map(SourceTask::getSource));
//        });
//    }
//
//    private static void assertKotlinPluginWasApplied(Project evaluatedProject) {
//        // We need a plugin to enable Kotlin compilation. The kotlin-dsl plugin seems to be useless for this plugin so we request the user to apply org.jetbrains.kotlin.jvm. We can apply the plugin ourselves as we will be lock to a specific version and require to pull the library even if someone is only using Java or Groovy. We could split the plugin into various Jar. We can explore this later.
//        if (evaluatedProject.getPluginManager().findPlugin("org.jetbrains.kotlin.jvm") == null) {
//            // TODO: Detect and suggest the Kotlin DSL way of applying the plugin
//            // TODO: Suggest the right version for the used Gradle
//            throw new GradleException("You need to manually apply the `org.jetbrains.kotlin.jvm` plugin inside the plugin block:\n" +
//                    "plugins {\n" +
//                    "    id(\"org.jetbrains.kotlin.jvm\") version \"1.3.50\"\n" +
//                    "}");
//        }
//    }
//}