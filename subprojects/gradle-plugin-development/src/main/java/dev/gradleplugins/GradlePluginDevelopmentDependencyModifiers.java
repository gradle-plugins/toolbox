package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.provider.Provider;

public interface GradlePluginDevelopmentDependencyModifiers {
    interface PlatformDependencyModifiers {
        DependencyModifier getPlatform();
        DependencyModifier getEnforcedPlatform();
    }

    interface TestFixturesDependencyModifiers {
        DependencyModifier getTestFixtures();
    }

    interface DependencyModifier {
        <DependencyType extends ModuleDependency> DependencyType modify(DependencyType dependency);
        ExternalModuleDependency modify(CharSequence dependencyNotation);
        ProjectDependency modify(Project project);
    }
}
