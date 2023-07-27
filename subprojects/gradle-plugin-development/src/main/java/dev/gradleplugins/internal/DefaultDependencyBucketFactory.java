package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

public final class DefaultDependencyBucketFactory implements DependencyBucketFactory {
    private final Project project;
    private final Provider<SourceSet> sourceSetProvider;

    public DefaultDependencyBucketFactory(Project project, Provider<SourceSet> sourceSetProvider) {
        this.project = project;
        this.sourceSetProvider = sourceSetProvider;
    }

    @Override
    public GradlePluginDevelopmentDependencyBucket create(String bucketName) {
        return new DefaultDependencyBucket(project, sourceSetProvider, bucketName);
    }
}
