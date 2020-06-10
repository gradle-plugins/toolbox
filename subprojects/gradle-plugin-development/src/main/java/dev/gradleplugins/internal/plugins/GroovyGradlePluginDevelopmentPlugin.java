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

import dev.gradleplugins.GradleRuntimeCompatibility;
import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GroovyGradlePluginDevelopmentExtension;
import dev.gradleplugins.internal.DeferredRepositoryFactory;
import dev.gradleplugins.internal.GradlePluginDevelopmentExtensionInternal;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import static dev.gradleplugins.internal.plugins.AbstractGradlePluginDevelopmentPlugin.*;

public class GroovyGradlePluginDevelopmentPlugin implements Plugin<Project> {
    private static final String PLUGIN_ID = "dev.gradleplugins.groovy-gradle-plugin";
    @Override
    public void apply(Project project) {
        assertOtherGradlePluginDevelopmentPluginsAreNeverApplied(project.getPluginManager(), PLUGIN_ID);
        assertJavaGradlePluginIsNotPreviouslyApplied(project.getPluginManager(), PLUGIN_ID);
        assertKotlinDslPluginIsNeverApplied(project.getPluginManager(), PLUGIN_ID);

        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);

        project.getPluginManager().apply("java-gradle-plugin"); // For plugin development
        removeGradleApiProjectDependency(project);
        project.getPluginManager().apply("groovy");

        registerLanguageExtension(project, "groovy", GroovyGradlePluginDevelopmentExtension.class);
        GradlePluginDevelopmentCompatibilityExtension extension = registerCompatibilityExtension(project);
        configureExtension(extension, project, repositoryFactory);

        project.getPluginManager().apply(GradlePluginDevelopmentFunctionalTestingPlugin.class);

        // TODO: Once lazy dependency is supported, see https://github.com/gradle/gradle/pull/11767
        // project.getDependencies().add("compileOnly", extension.getMinimumGradleVersion().map(VersionNumber::parse).map(GroovyGradlePluginDevelopmentPlugin::toGroovyVersion).map(version -> "org.codehaus.groovy:groovy:" + version));
        project.afterEvaluate(proj -> {
            project.getDependencies().add("compileOnly", "org.codehaus.groovy:groovy-all:" + extension.getMinimumGradleVersion().map(GradleRuntimeCompatibility::groovyVersionOf).get());
        });

        repositoryFactory.groovy();

        // TODO: warn if the plugin only have has Java source and no Groovy.
        //   You specified a Groovy plugin development so we should expect Gradle plugin to be all in Groovy
        //   We could ensure GradlePlugin aren't applied to any Java source
        //   We could also check that no plugin id on `gradlePlugin` container points to a Java source
        //   We could do the same for Kotlin code
    }
}