package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

public interface GradlePluginDevelopmentDependencyBucket extends Named {
    void add(Dependency dependency);

    <DependencyType extends Dependency> void add(DependencyType dependency, Action<? super DependencyType> configureAction);

    <DependencyType extends Dependency> void add(Provider<DependencyType> dependencyProvider);

    <DependencyType extends Dependency> void add(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction);

    void add(FileCollection fileCollection);

    void add(Project project);

    void add(CharSequence dependencyNotation);

    void add(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction);

    Provider<Configuration> getAsConfiguration();
}
