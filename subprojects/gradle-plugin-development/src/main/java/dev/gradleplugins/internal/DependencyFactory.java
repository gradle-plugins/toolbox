package dev.gradleplugins.internal;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.file.FileCollection;

public final class DependencyFactory {
    private final DependencyHandler dependencies;

    public DependencyFactory(DependencyHandler dependencies) {
        this.dependencies = dependencies;
    }

    public Dependency create(Object notation) {
        return dependencies.create(notation);
    }

    public ExternalModuleDependency create(CharSequence notation) {
        assert notation != null : "'notation' must not be null";
        return (ExternalModuleDependency) dependencies.create(notation);
    }

    public FileCollectionDependency create(FileCollection fileCollection) {
        assert fileCollection != null : "'fileCollection' must not be null";
        return (FileCollectionDependency) dependencies.create(fileCollection);
    }

    public ProjectDependency create(Project project) {
        assert project != null : "'project' must not be null";
        return (ProjectDependency) dependencies.create(project);
    }

    public ExternalModuleDependency gradleApi(String version) {
        assert version != null : "'version' must not be null";
        return create("dev.gradleplugins:gradle-api:" + version);
    }

    public ExternalModuleDependency gradleTestKit(String version) {
        assert version != null : "'version' must not be null";
        return create("dev.gradleplugins:gradle-test-kit:" + version);
    }

    public SelfResolvingDependency localGradleTestKit() {
        return (SelfResolvingDependency) dependencies.gradleTestKit();
    }

    public SelfResolvingDependency localGradleApi() {
        return (SelfResolvingDependency) dependencies.gradleApi();
    }

    public ExternalModuleDependency gradleFixtures() {
        final ExternalModuleDependency result = create("dev.gradleplugins:gradle-fixtures:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
        result.capabilities(it -> {
            it.requireCapability("dev.gradleplugins:gradle-fixtures-spock-support");
        });
        return result;
    }

    public ExternalModuleDependency gradleRunnerKit() {
        return create("dev.gradleplugins:gradle-runner-kit:" + DefaultDependencyVersions.GRADLE_FIXTURES_VERSION);
    }

    public ExternalModuleDependency groovy(String version) {
        assert version != null : "'version' must not be null";
        return create("org.codehaus.groovy:groovy-all:" + version);
    }

    @Deprecated
    public ExternalModuleDependency spockFramework() {
        return create("org.spockframework:spock-core");
    }

    @Deprecated
    public ExternalModuleDependency spockFramework(String version) {
        assert version != null : "'version' must not be null";
        return create("org.spockframework:spock-core:" + version);
    }

    @Deprecated
    public ExternalModuleDependency spockFrameworkPlatform(String version) {
        assert version != null : "'version' must not be null";
        return (ExternalModuleDependency) dependencies.platform(create("org.spockframework:spock-bom:" + version));
    }

    public static DependencyFactory forProject(Project project) {
        return new DependencyFactory(project.getDependencies());
    }
}
