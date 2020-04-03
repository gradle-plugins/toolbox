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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;

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

        configureDefaultJavaCompatibility(project.getExtensions().getByType(JavaPluginExtension.class));

        project.getPluginManager().apply(GradlePluginDevelopmentFunctionalTestingPlugin.class);

//        project.getRepositories().jcenter();
        project.getDependencies().add("compileOnly", "org.codehaus.groovy:groovy:2.5.2"); // require jcenter()
        project.getRepositories().mavenCentral(repo -> {
            repo.mavenContent(content -> {
                content.includeVersion("org.codehaus.groovy", "groovy", "2.5.2");

//                // for groovy-ant
//                content.includeVersion("org.apache.ant", "ant", "1.9.13");
//                content.includeVersion("org.apache.ant", "ant-launcher", "1.9.13");
//                content.includeVersion("org.apache.ant", "ant-parent", "1.9.13");
//
//                // for groovy-cli-commons
//                content.includeVersion("commons-cli", "commons-cli", "1.4");
//                content.includeVersion("org.apache.commons", "commons-parent", "42");
//                content.includeVersion("org.apache", "apache", "18");
//
//                // for groovy-cli-picocli
//                content.includeVersion("info.picocli", "picocli", "3.7.0");
//
//                // for groovy-docgenerator
//                content.includeVersion("com.thoughtworks.qdox", "qdox", "1.12.1");
//                content.includeVersion("org.codehaus", "codehaus-parent", "4");
//
//                // for groovy-groovysh
//                content.includeVersion("jline", "jline", "2.14.6");
//                content.includeVersion("org.sonatype.oss", "oss-parent", "9");
//
//                // for groovy-test
//                content.includeVersion("junit", "junit", "4.12");
//                content.includeVersion("org.hamcrest", "hamcrest-core", "1.3");
//                content.includeVersion("org.hamcrest", "hamcrest-parent", "1.3");
//
//                // for groovy-test-junit5
//                content.includeVersion("org.junit.platform", "junit-platform-launcher", "1.3.1");
//                content.includeVersion("org.apiguardian", "apiguardian-api", "1.0.0");
//                content.includeVersion("org.junit.platform", "junit-platform-engine", "1.3.1");
//                content.includeVersion("org.junit.platform", "junit-platform-commons", "1.3.1");
//                content.includeVersion("org.opentest4j", "opentest4j", "1.1.1");
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