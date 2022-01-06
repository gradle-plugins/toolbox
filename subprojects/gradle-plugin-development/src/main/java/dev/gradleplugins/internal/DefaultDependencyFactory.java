package dev.gradleplugins.internal;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

final class DefaultDependencyFactory implements DependencyFactory {
    private final DependencyHandler dependencies;

    DefaultDependencyFactory(DependencyHandler dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public Dependency create(Object notation) {
        return dependencies.create(notation);
    }
}
