package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class GradlePluginDevelopmentDependencyExtensionInternal implements GradlePluginDevelopmentDependencyExtension {
    private static final Logger LOGGER = Logging.getLogger(GradlePluginDevelopmentDependencyExtensionInternal.class);
    @Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencies;

    @Override
    public Dependency gradleApi(String version) {
        return getDependencies().create("dev.gradleplugins:gradle-api:" + version);
    }

    public static String gradleApiNotation(String version) {
        return "dev.gradleplugins:gradle-api:" + version;
    }

    @Override
    public Dependency gradleFixtures() {
        return getDependencies().create("dev.gradleplugins:gradle-fixtures:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
    }

    public void applyTo(DependencyHandler dependencies) {
        ExtensionAware.class.cast(dependencies).getExtensions().add(GradlePluginDevelopmentDependencyExtension.class, "gradlePluginDevelopment", this);
        try {
            Method target = Class.forName("dev.gradleplugins.internal.dsl.groovy.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
            target.invoke(null, dependencies, "gradleApi", new GradleApiClosure(dependencies));
            target.invoke(null, dependencies, "gradleFixtures", new GradleFixturesClosure(dependencies));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.info("Unable to extend DependencyHandler with gradleApi(String) and gradleFixtures().");
        }
    }

    private class GradleApiClosure extends Closure<Dependency> {
        public GradleApiClosure(Object owner) {
            super(owner);
        }

        public Dependency doCall(String version) {
            return GradlePluginDevelopmentDependencyExtensionInternal.this.gradleApi(version);
        }
    }

    private class GradleFixturesClosure extends Closure<Dependency> {
        public GradleFixturesClosure(Object owner) {
            super(owner);
        }

        public Dependency doCall() {
            return GradlePluginDevelopmentDependencyExtensionInternal.this.gradleFixtures();
        }
    }
}
