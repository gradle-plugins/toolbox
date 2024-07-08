package dev.gradleplugins;

import dev.gradleplugins.internal.DependencyFactory;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

final class DefaultGradlePluginDevelopmentDependencyExtension implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
    private static final String LOCAL_GRADLE_VERSION = "local";
    private final DependencyFactory factory;

    DefaultGradlePluginDevelopmentDependencyExtension(DependencyHandler dependencies) {
        this.factory = new DependencyFactory(dependencies);
    }

    @Override
    public Dependency gradleApi(String version) {
        if (LOCAL_GRADLE_VERSION.equals(version)) {
            return factory.localGradleApi();
        }
        return factory.gradleApi(version);
    }

    @Override
    public Dependency gradleTestKit(String version) {
        if (LOCAL_GRADLE_VERSION.equals(version)) {
            return factory.localGradleTestKit();
        }
        return factory.gradleTestKit(version);
    }

    @Override
    public Dependency gradleFixtures() {
        return factory.gradleFixtures();
    }

    @Override
    public Dependency gradleRunnerKit() {
        return factory.gradleRunnerKit();
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
    }
}
