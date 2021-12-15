package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class RegisterGradlePluginDevelopmentDependencyExtensionRule implements Action<Project> {
    private static final Logger LOGGER = Logging.getLogger(RegisterGradlePluginDevelopmentDependencyExtensionRule.class);

    @Override
    public void execute(Project project) {
        final DependencyHandler dependencies = project.getDependencies();
        try {
            Method target = Class.forName("dev.gradleplugins.internal.dsl.groovy.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
            target.invoke(null, dependencies, "gradleApi", new GradleApiClosure(dependencies));
            target.invoke(null, dependencies, "gradleTestKit", new GradleTestKitClosure(dependencies));
            target.invoke(null, dependencies, "gradleFixtures", new GradleFixturesClosure(dependencies));
            target.invoke(null, dependencies, "gradleRunnerKit", new GradleRunnerKitClosure(dependencies));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.info("Unable to extend DependencyHandler with gradleApi(String) and gradleFixtures().");
        }
    }

    private static class GradleApiClosure extends Closure<Dependency> {
        private final GradlePluginDevelopmentDependencyExtension extension;

        public GradleApiClosure(DependencyHandler handler) {
            super(handler);
            this.extension = GradlePluginDevelopmentDependencyExtension.from(handler);
        }

        public Dependency doCall(String version) {
            return extension.gradleApi(version);
        }
    }

    private static class GradleTestKitClosure extends Closure<Dependency> {
        private final GradlePluginDevelopmentDependencyExtension extension;

        public GradleTestKitClosure(DependencyHandler handler) {
            super(handler);
            this.extension = GradlePluginDevelopmentDependencyExtension.from(handler);
        }

        public Dependency doCall(String version) {
            return extension.gradleTestKit(version);
        }
    }

    private static class GradleFixturesClosure extends Closure<Dependency> {
        private final GradlePluginDevelopmentDependencyExtension extension;

        public GradleFixturesClosure(DependencyHandler handler) {
            super(handler);
            this.extension = GradlePluginDevelopmentDependencyExtension.from(handler);
        }

        public Dependency doCall() {
            return extension.gradleFixtures();
        }
    }

    private static class GradleRunnerKitClosure extends Closure<Dependency> {
        private final GradlePluginDevelopmentDependencyExtension extension;

        public GradleRunnerKitClosure(DependencyHandler handler) {
            super(handler);
            this.extension = GradlePluginDevelopmentDependencyExtension.from(handler);
        }

        public Dependency doCall() {
            return extension.gradleRunnerKit();
        }
    }
}
