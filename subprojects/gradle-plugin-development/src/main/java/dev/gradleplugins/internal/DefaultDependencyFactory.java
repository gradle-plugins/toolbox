package dev.gradleplugins.internal;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;

final class DefaultDependencyFactory implements DependencyFactory {
    private final DependencyHandler dependencies;

    DefaultDependencyFactory(DependencyHandler dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public ExternalModuleDependency create(CharSequence notation) {
        assert notation != null : "'notation' must not be null";
        return (ExternalModuleDependency) dependencies.create(notation);
    }

    @Override
    public FileCollectionDependency create(FileCollection fileCollection) {
        assert fileCollection != null : "'fileCollection' must not be null";
        return (FileCollectionDependency) dependencies.create(fileCollection);
    }

    @Override
    public ProjectDependency create(Project project) {
        assert project != null : "'project' must not be null";
        return (ProjectDependency) dependencies.create(project);
    }

    @Override
    public ExternalModuleDependency gradleApi(String version) {
        assert version != null : "'version' must not be null";
        return (ExternalModuleDependency) dependencies.create("dev.gradleplugins:gradle-api:" + version);
    }

    @Override
    public ExternalModuleDependency gradleTestKit(String version) {
        assert version != null : "'version' must not be null";
        return (ExternalModuleDependency) dependencies.create("dev.gradleplugins:gradle-test-kit:" + version);
    }

    @Override
    public SelfResolvingDependency localGradleApi() {
        return (SelfResolvingDependency) dependencies.gradleApi();
    }

    @Override
    public SelfResolvingDependency localGradleTestKit() {
        return (SelfResolvingDependency) dependencies.gradleTestKit();
    }

    @Override
    public Dependency gradleFixtures() {
        final ModuleDependency dependency = (ModuleDependency) dependencies.create("dev.gradleplugins:gradle-fixtures:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
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
    public ExternalModuleDependency gradlePlugin(String notation) {
        assert notation != null : "'notation' must not be null";

        // Parsing <plugin-id>[:<version>]
        String pluginId = null;
        String version = null;
        {
            int index = notation.indexOf(':');
            if (index == -1) {
                pluginId = notation;
            } else if (notation.indexOf(':', index + 1) != -1) {
                throw new RuntimeException("Invalid Gradle plugin notation, please use '<plugin-id>' or '<plugin-id>:<version>'.");
            } else {
                pluginId = notation.substring(0, index);
                version = notation.substring(index + 1);
            }
        }

        // Dependency
        if (version == null) {
            return (ExternalModuleDependency) dependencies.create(pluginId + ":" + pluginId + ".gradle.plugin");
        } else {
            return (ExternalModuleDependency) dependencies.create(pluginId + ":" + pluginId + ".gradle.plugin:" + version);
        }
    }
}
