package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.tasks.SourceSet;

import java.util.function.Supplier;

final class PluginUnderTestMetadataConfigurationSupplier implements Supplier<NamedDomainObjectProvider<Configuration>> {
    private final Project project;
    private final GradlePluginDevelopmentTestSuite testSuite;
    private NamedDomainObjectProvider<Configuration> pluginUnderTestMetadata;

    public PluginUnderTestMetadataConfigurationSupplier(Project project, GradlePluginDevelopmentTestSuite testSuite) {
        this.project = project;
        this.testSuite = testSuite;
    }

    private SourceSet sourceSet() {
        return testSuite.getSourceSet().get();
    }

    @Override
    public NamedDomainObjectProvider<Configuration> get() {
        if (pluginUnderTestMetadata == null) {
            final Configuration configuration = project.getConfigurations().maybeCreate(sourceSet().getName() + "PluginUnderTestMetadata");
            configuration.setCanBeResolved(true);
            configuration.setCanBeConsumed(false);
            configuration.attributes(attributes -> attributes.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_RUNTIME)));
            configuration.setDescription("Plugin under test metadata for source set '" + sourceSet().getName() + "'.");
            pluginUnderTestMetadata = project.getConfigurations().named(configuration.getName());
        }
        return pluginUnderTestMetadata;
    }
}
