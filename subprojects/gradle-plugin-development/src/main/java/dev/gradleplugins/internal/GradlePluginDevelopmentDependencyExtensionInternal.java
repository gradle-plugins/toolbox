package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class GradlePluginDevelopmentDependencyExtensionInternal implements GradlePluginDevelopmentDependencyExtension {
    private static final Logger LOGGER = Logging.getLogger(GradlePluginDevelopmentDependencyExtensionInternal.class);
    @Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencies;
    private final Project project; // for the provider as notation shim

    @Override
    public Dependency gradleApi(String version) {
        return getDependencies().create("dev.gradleplugins:gradle-api:" + version);
    }

    @Override
    public Dependency gradleFixtures() {
        return getDependencies().create("dev.gradleplugins:gradle-fixtures:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
    }

    public Dependency groovy(String version) {
        return getDependencies().create("org.codehaus.groovy:groovy-all:" + version);
    }

    public Dependency spockFramework(String version) {
        return getDependencies().create("org.spockframework:spock-core:" + version);
    }

    public Dependency spockFramework() {
        return getDependencies().create("org.spockframework:spock-core");
    }

    public Dependency spockFrameworkPlatform(String version) {
        return getDependencies().platform(getDependencies().create("org.spockframework:spock-bom:" + version));
    }

    // Shim for supporting older Gradle versions
    public void add(Project project, String configuration, Provider<Object> notation) {
        if (isGradleVersionGreaterOrEqualsTo6Dot5()) {
            getDependencies().add(configuration, notation);
        } else {
            project.afterEvaluate(proj -> getDependencies().add(configuration, notation.get()));
        }
    }

    public void add(String configuration, Provider<Object> notation) {
        if (isGradleVersionGreaterOrEqualsTo6Dot5()) {
            getDependencies().add(configuration, notation);
        } else {
            project.afterEvaluate(proj -> getDependencies().add(configuration, notation.get()));
        }
    }

    public void add(String configuration, Object notation) {
        getDependencies().add(configuration, notation);
    }

    private static boolean isGradleVersionGreaterOrEqualsTo6Dot5() {
        return GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0;
    }

    public static GradlePluginDevelopmentDependencyExtensionInternal of(DependencyHandler dependencies) {
        return (GradlePluginDevelopmentDependencyExtensionInternal) ExtensionAware.class.cast(dependencies).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class);
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
