package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;

public final class TestFixturesDependencyModifier implements GradlePluginDevelopmentDependencyModifiers.DependencyModifier {
    private final Project project;
    private final DependencyFactory dependencyFactory;

    public TestFixturesDependencyModifier(Project project) {
        this.project = project;
        this.dependencyFactory = DependencyFactory.forProject(project);
    }

    @Override
    public <DependencyType extends ModuleDependency> DependencyType modify(DependencyType dependency) {
        @SuppressWarnings("unchecked")
        final DependencyType result = (DependencyType) project.getDependencies().testFixtures(dependency);
        return result;
    }

    @Override
    public ExternalModuleDependency modify(CharSequence dependencyNotation) {
        return modify(dependencyFactory.create(dependencyNotation));
    }

    @Override
    public ProjectDependency modify(Project project) {
        return modify(dependencyFactory.create(project));
    }
}
