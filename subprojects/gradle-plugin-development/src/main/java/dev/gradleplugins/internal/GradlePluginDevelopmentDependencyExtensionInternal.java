package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public class GradlePluginDevelopmentDependencyExtensionInternal implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
    private final GradlePluginDevelopmentDependencyExtension extension;
    private final DependencyFactory factory;

    @Inject
    public GradlePluginDevelopmentDependencyExtensionInternal(GradlePluginDevelopmentDependencyExtension extension, DependencyFactory factory) {
        this.extension = extension;
        this.factory = factory;
    }

    @Override
    public Dependency gradleApi(String version) {
        return extension.gradleApi(version);
    }

    @Override
    public Dependency gradleTestKit(String version) {
        return extension.gradleTestKit(version);
    }

    @Override
    public Dependency gradleFixtures() {
        return extension.gradleFixtures();
    }

    @Override
    public Dependency gradleRunnerKit() {
        return factory.gradleRunnerKit();
    }

    public Dependency groovy(String version) {
        return factory.groovy(version);
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
    }
}
