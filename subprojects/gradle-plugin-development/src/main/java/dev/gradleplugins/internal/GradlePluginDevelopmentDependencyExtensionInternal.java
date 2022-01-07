package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public class GradlePluginDevelopmentDependencyExtensionInternal implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
    private final DependencyHandler dependencies;
    private final GradlePluginDevelopmentDependencyExtension extension;
    private final ConfigurationContainer configurations;
    private final DependencyFactory factory;

    @Inject
    public GradlePluginDevelopmentDependencyExtensionInternal(DependencyHandler dependencies, GradlePluginDevelopmentDependencyExtension extension, ConfigurationContainer configurations, DependencyFactory factory) {
        this.dependencies = dependencies;
        this.extension = extension;
        this.configurations = configurations;
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
        return extension.gradleRunnerKit();
    }

    public Dependency groovy(String version) {
        return factory.create("org.codehaus.groovy:groovy-all:" + version);
    }

    public Dependency spockFramework(String version) {
        return factory.create("org.spockframework:spock-core:" + version);
    }

    // Used by SpockFrameworkTestSuiteBasePlugin
    public Dependency spockFramework() {
        return factory.create("org.spockframework:spock-core");
    }

    // Used by SpockFrameworkTestSuiteBasePlugin
    public Dependency spockFrameworkPlatform(String version) {
        return dependencies.platform(factory.create("org.spockframework:spock-bom:" + version));
    }

    // Shim for supporting older Gradle versions
    public void add(String configuration, Provider<Object> notation) {
        configurations.named(configuration, new AddDependency(notation, factory));
    }

    public void add(String configuration, Object notation) {
        configurations.named(configuration, new AddDependency(notation, factory));
    }

    public static GradlePluginDevelopmentDependencyExtensionInternal of(DependencyHandler dependencies) {
        return (GradlePluginDevelopmentDependencyExtensionInternal) ExtensionAware.class.cast(dependencies).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class);
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
    }
}
