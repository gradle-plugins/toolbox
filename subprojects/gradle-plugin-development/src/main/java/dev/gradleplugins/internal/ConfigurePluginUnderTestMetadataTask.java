package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.ClasspathNormalizer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.component.local.model.OpaqueComponentIdentifier;
import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata;

import java.util.Collections;

import static org.gradle.api.internal.artifacts.dsl.dependencies.DependencyFactory.ClassPathNotation;

public final class ConfigurePluginUnderTestMetadataTask implements Action<GradlePluginDevelopmentTestSuite> {
    private final Project project;

    public ConfigurePluginUnderTestMetadataTask(Project project) {
        this.project = project;
    }

    @Override
    public void execute(GradlePluginDevelopmentTestSuite testSuite) {
        final Provider<Configuration> pluginUnderTestMetadata = testSuite.getDependencies().getPluginUnderTestMetadata().getAsConfiguration();
        testSuite.getPluginUnderTestMetadataTask().configure(task -> {
            task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir(task.getName()));
            task.getPluginClasspath().from(testSuite.getTestedSourceSet().map(asPluginClasspath(project)).orElse(Collections.emptyList()));
            task.getPluginClasspath().from(pluginUnderTestMetadata);
        });

        ignorePluginUnderTestMetadataFile(project);
        configurePluginUnderTestMetadataAsTestInputs(testSuite);
        configurePluginUnderTestMetadataAsRuntimeOnlyDependencies(project, testSuite);
    }

    private static void configurePluginUnderTestMetadataAsRuntimeOnlyDependencies(Project project, GradlePluginDevelopmentTestSuite testSuite) {
        // Consider adding gradleTestKit to implementation as per java-gradle-plugin
        testSuite.getDependencies().getRuntimeOnly().add(project.getLayout().files(testSuite.getPluginUnderTestMetadataTask()));
    }

    private static void configurePluginUnderTestMetadataAsTestInputs(GradlePluginDevelopmentTestSuite testSuite) {
        testSuite.getTestTasks().configureEach(task -> task.getInputs()
                .files(testSuite.getPluginUnderTestMetadataTask().map(PluginUnderTestMetadata::getPluginClasspath))
                .withPropertyName("pluginUnderTestClasspath") // different from JavaGradlePluginPlugin, e.g. pluginClasspath, to avoid conflict
                .withNormalizer(ClasspathNormalizer.class));
    }

    private static void ignorePluginUnderTestMetadataFile(Project project) {
        project.getNormalization().getRuntimeClasspath().ignore(PluginUnderTestMetadata.METADATA_FILE_NAME);
    }

    private static Transformer<Object, SourceSet> asPluginClasspath(Project project) {
        return sourceSet -> {
            val runtimeClasspath = project.getConfigurations().getByName(sourceSet.getRuntimeClasspathConfigurationName());
            val view = runtimeClasspath.getIncoming().artifactView(config -> {
                config.componentFilter(componentId -> {
                    if (componentId instanceof OpaqueComponentIdentifier) {
                        return !componentId.getDisplayName().equals(ClassPathNotation.GRADLE_API.displayName)
                                && !componentId.getDisplayName().equals(ClassPathNotation.LOCAL_GROOVY.displayName);
                    }
                    return true;
                });
            });
            return sourceSet.getOutput().plus(view.getFiles());
        };
    }
}
