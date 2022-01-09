package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Project;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.sourceSets;

final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
    private final Project project;

    DefaultGradlePluginDevelopmentTestSuiteFactory(Project project) {
        this.project = project;
    }

    @Override
    public GradlePluginDevelopmentTestSuite create(String name) {
        val result = project.getObjects().newInstance(GradlePluginDevelopmentTestSuiteInternal.class, name);
        result.getSourceSet().convention(project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-base")) {
                return sourceSets(project).maybeCreate(name);
            } else {
                throw new RuntimeException("Please apply 'java-base' plugin.");
            }
        }));
        result.getTestedSourceSet().convention(project.provider(() -> {
            if (project.getPluginManager().hasPlugin("java-gradle-plugin")) {
                return gradlePlugin(project).getPluginSourceSet();
            }
            return null;
        }));
        return result;
    }
}
