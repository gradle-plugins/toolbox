package dev.gradleplugins.internal;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public interface DependencyFactory {
    Dependency create(Object notation);

    static DependencyFactory forProject(Project project) {
        return new DefaultDependencyFactory(project.getDependencies());
    }
}
