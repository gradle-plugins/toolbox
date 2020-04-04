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
import dev.gradleplugins.internal.DeferredRepositoryFactory;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.util.GradleVersion;
import org.gradle.util.VersionNumber;

import static dev.gradleplugins.internal.plugins.AbstractGradlePluginDevelopmentPlugin.*;

public class GroovyGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.groovy-gradle-plugin";
    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), PLUGIN_ID);
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), PLUGIN_ID);
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), PLUGIN_ID);

        project.getPluginManager().apply("java-gradle-plugin"); // For plugin development
        removeGradleApiProjectDependency(project);
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
        configureGradleApiDependencies(project, extension.getMinimumGradleVersion());

        project.getPluginManager().apply(GradlePluginDevelopmentFunctionalTestingPlugin.class);

        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);

        // TODO: Once lazy dependency is supported, see https://github.com/gradle/gradle/pull/11767
        // project.getDependencies().add("compileOnly", extension.getMinimumGradleVersion().map(VersionNumber::parse).map(GroovyGradlePluginDevelopmentPlugin::toGroovyVersion).map(version -> "org.codehaus.groovy:groovy:" + version));
        project.afterEvaluate(proj -> {
            project.getDependencies().add("compileOnly", "org.codehaus.groovy:groovy-all:" + extension.getMinimumGradleVersion().map(VersionNumber::parse).map(GroovyGradlePluginDevelopmentPlugin::toGroovyVersion).get());
        });

        repositoryFactory.groovy();

        // TODO: warn if the plugin only have has Java source and no Groovy.
        //   You specified a Groovy plugin development so we should expect Gradle plugin to be all in Groovy
        //   We could ensure GradlePlugin aren't applied to any Java source
        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
        //   We could do the same for Kotlin code
    }

    private static String toGroovyVersion(VersionNumber version) {
        // Use `find ~/.gradle/wrapper -name "groovy-all-*"`
        // TODO: Complete this mapping
        switch (String.format("%d.%d", version.getMajor(), version.getMinor())) {
            case "1.12":
                return "1.8.6";
            case "2.14":
                return "2.4.4";
            case "3.0":
                return "2.4.7";
            case "3.5":
                return "2.4.10";
            case "4.0":
                return "2.4.11";
            case "4.3":
                return "2.4.12";
            case "5.0":
            case "5.1":
            case "5.2":
            case "5.3":
            case "5.4":
            case "5.5":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.0-2.5.4";
            case "5.6":
                return "2.5.4"; //"org.gradle.groovy:groovy-all:1.3-2.5.4";
            case "6.0":
            case "6.1":
            case "6.2":
                return "2.5.8"; //"org.gradle.groovy:groovy-all:1.3-2.5.8";
            case "6.3":
                return "2.5.10"; //"org.gradle.groovy:groovy-all:1.3-2.5.10";
            default:
                throw new IllegalArgumentException("Version not known at the time, please check groovy-all version");
        }
    }
}