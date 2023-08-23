package dev.gradleplugins.internal;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.file.FileCollection;

public interface DependencyFactory {
    ExternalModuleDependency create(CharSequence notation);
    FileCollectionDependency create(FileCollection fileCollection);
    ProjectDependency create(Project project);

    Dependency gradleApi(String version);
    Dependency gradleTestKit(String version);
    SelfResolvingDependency localGradleApi();
    SelfResolvingDependency localGradleTestKit();

    @Deprecated
    Dependency gradleFixtures();

    Dependency gradleRunnerKit();

    ExternalModuleDependency gradlePlugin(String notation);

    static DependencyFactory forProject(Project project) {
        return new DefaultDependencyFactory(project.getDependencies());
    }
}
