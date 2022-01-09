package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;

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
                return project.getExtensions().getByType(SourceSetContainer.class).maybeCreate(name);
            } else {
                throw new RuntimeException("Please apply 'java-base' plugin.");
            }
        }));
        return result;
    }
}
