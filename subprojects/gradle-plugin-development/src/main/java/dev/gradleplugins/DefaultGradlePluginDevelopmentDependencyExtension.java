package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultDependencyVersions;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import java.util.Objects;

final class DefaultGradlePluginDevelopmentDependencyExtension implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
    private static final String LOCAL_GRADLE_VERSION = "local";
    private final DependencyHandler dependencies;

    DefaultGradlePluginDevelopmentDependencyExtension(DependencyHandler dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public Dependency gradleApi(String version) {
        if (LOCAL_GRADLE_VERSION.equals(version)) {
            return dependencies.gradleApi();
        }
        return dependencies.create("dev.gradleplugins:gradle-api:" + Objects.requireNonNull(version));
    }

    @Override
    public Dependency gradleTestKit(String version) {
        if (LOCAL_GRADLE_VERSION.equals(version)) {
            return dependencies.gradleTestKit();
        }
        return dependencies.create("dev.gradleplugins:gradle-test-kit:" + Objects.requireNonNull(version));
    }

    @Override
    public Dependency gradleFixtures() {
        ModuleDependency dependency = (ModuleDependency)dependencies.create("dev.gradleplugins:gradle-fixtures:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
        dependency.capabilities(it -> {
            it.requireCapability("dev.gradleplugins:gradle-fixtures-spock-support");
        });
        return dependency;
    }

    @Override
    public Dependency gradleRunnerKit() {
        return dependencies.create("dev.gradleplugins:gradle-runner-kit:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
    }
}
