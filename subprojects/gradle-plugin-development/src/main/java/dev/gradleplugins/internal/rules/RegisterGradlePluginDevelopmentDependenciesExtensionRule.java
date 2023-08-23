package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentDependencies;
import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers;
import dev.gradleplugins.internal.DefaultDependencyBucket;
import dev.gradleplugins.internal.DefaultDependencyBucketFactory;
import dev.gradleplugins.internal.DependencyBucketFactory;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.EnforcedPlatformDependencyModifier;
import dev.gradleplugins.internal.PlatformDependencyModifier;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static dev.gradleplugins.internal.DefaultDependencyBucket.pluginSourceSet;

public final class RegisterGradlePluginDevelopmentDependenciesExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        assert project.getPluginManager().hasPlugin("java-gradle-plugin");

        gradlePlugin(project, extension -> {
            final DefaultGradlePluginDevelopmentDependencies dependenciesExtension = project.getObjects().newInstance(DefaultGradlePluginDevelopmentDependencies.class, project, new DefaultDependencyBucketFactory(project, pluginSourceSet(project)));

            // adhoc decoration of the dependencies
            dependenciesExtension.forEach(dependencyBucket -> {
                GroovyHelper.instance().addNewInstanceMethod(dependenciesExtension, dependencyBucket.getName(), new MethodClosure(dependencyBucket, "add"));
            });
            GroovyHelper.instance().addNewInstanceMethod(dependenciesExtension, "platform", new MethodClosure(dependenciesExtension.getPlatform(), "modify"));
            GroovyHelper.instance().addNewInstanceMethod(dependenciesExtension, "enforcedPlatform", new MethodClosure(dependenciesExtension.getEnforcedPlatform(), "modify"));

            ((ExtensionAware) extension).getExtensions().add(GradlePluginDevelopmentDependencies.class, "dependencies", dependenciesExtension);
        });
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> action) {
        action.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }

    protected static abstract /*final*/ class DefaultGradlePluginDevelopmentDependencies implements GradlePluginDevelopmentDependencies, Iterable<GradlePluginDevelopmentDependencyBucket> {
        private final Map<String, GradlePluginDevelopmentDependencyBucket> dependencyBuckets = new LinkedHashMap<>();
        private final DependencyFactory dependencyFactory;
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier platformDependencyModifier;
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier enforcedPlatformDependencyModifier;
        private final Project project;

        @Inject
        public DefaultGradlePluginDevelopmentDependencies(Project project, DependencyBucketFactory dependencyBucketFactory) {
            this.dependencyFactory = DependencyFactory.forProject(project);
            this.platformDependencyModifier = new PlatformDependencyModifier(project);
            this.enforcedPlatformDependencyModifier = new EnforcedPlatformDependencyModifier(project);
            this.project = project;
            add(dependencyBucketFactory.create("api"));
            add(dependencyBucketFactory.create("implementation"));
            add(dependencyBucketFactory.create("compileOnly"));
            add(dependencyBucketFactory.create("runtimeOnly"));
            add(dependencyBucketFactory.create("annotationProcessor"));
        }

        private void add(GradlePluginDevelopmentDependencyBucket dependencyBucket) {
            dependencyBuckets.put(dependencyBucket.getName(), dependencyBucket);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getApi() {
            return dependencyBuckets.get("api");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getImplementation() {
            return dependencyBuckets.get("implementation");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getCompileOnly() {
            return dependencyBuckets.get("compileOnly");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getRuntimeOnly() {
            return dependencyBuckets.get("runtimeOnly");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getAnnotationProcessor() {
            return dependencyBuckets.get("annotationProcessor");
        }

        @Override
        public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getPlatform() {
            return platformDependencyModifier;
        }

        @Override
        public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getEnforcedPlatform() {
            return enforcedPlatformDependencyModifier;
        }

        @Override
        public Dependency gradleApi(String version) {
            if ("local".equals(version)) {
                return dependencyFactory.localGradleApi();
            }
            return dependencyFactory.gradleApi(version);
        }

        @Override
        public ProjectDependency project(String projectPath) {
            return dependencyFactory.create(project.project(projectPath));
        }

        @Override
        public ProjectDependency project() {
            return dependencyFactory.create(project);
        }

        @Override
        public ExternalModuleDependency gradlePlugin(String pluginNotation) {
            return dependencyFactory.gradlePlugin(pluginNotation);
        }

        @Override
        public Iterator<GradlePluginDevelopmentDependencyBucket> iterator() {
            return dependencyBuckets.values().iterator();
        }
    }
}
