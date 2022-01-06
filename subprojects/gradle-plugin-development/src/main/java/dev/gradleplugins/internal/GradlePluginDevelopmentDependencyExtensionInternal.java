package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

public class GradlePluginDevelopmentDependencyExtensionInternal implements GradlePluginDevelopmentDependencyExtension {
    @Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencies;
    private final Project project; // for the provider as notation shim
    private final GradlePluginDevelopmentDependencyExtension extension;
    private final ConfigurationContainer configurations;

    @Inject
    public GradlePluginDevelopmentDependencyExtensionInternal(DependencyHandler dependencies, Project project, GradlePluginDevelopmentDependencyExtension extension, ConfigurationContainer configurations) {
        this.dependencies = dependencies;
        this.project = project;
        this.extension = extension;
        this.configurations = configurations;
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
        return extension.gradleRunnerKit();
    }

    public Dependency groovy(String version) {
        return getDependencies().create("org.codehaus.groovy:groovy-all:" + version);
    }

    public Dependency spockFramework(String version) {
        return getDependencies().create("org.spockframework:spock-core:" + version);
    }

    // Used by SpockFrameworkTestSuiteBasePlugin
    public Dependency spockFramework() {
        return getDependencies().create("org.spockframework:spock-core");
    }

    // Used by SpockFrameworkTestSuiteBasePlugin
    public Dependency spockFrameworkPlatform(String version) {
        return getDependencies().platform(getDependencies().create("org.spockframework:spock-bom:" + version));
    }

    // Shim for supporting older Gradle versions
    public void add(Project project, String configuration, Provider<Object> notation) {
        configurations.named(configuration, new AddDependency(notation, getDependencies()::create));
    }

    public void add(String configuration, Provider<Object> notation) {
        configurations.named(configuration, new AddDependency(notation, getDependencies()::create));
    }

    public void add(String configuration, Object notation) {
        configurations.named(configuration, new AddDependency(notation, getDependencies()::create));
    }

    public static GradlePluginDevelopmentDependencyExtensionInternal of(DependencyHandler dependencies) {
        return (GradlePluginDevelopmentDependencyExtensionInternal) ExtensionAware.class.cast(dependencies).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class);
    }
}
