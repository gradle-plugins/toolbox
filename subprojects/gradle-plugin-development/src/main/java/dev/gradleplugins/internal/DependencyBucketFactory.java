package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;

public interface DependencyBucketFactory {
    GradlePluginDevelopmentDependencyBucket create(String bucketName);
}
