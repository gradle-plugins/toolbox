package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import org.gradle.api.model.ObjectFactory;

final class DefaultGradlePluginDevelopmentTestSuiteFactory implements GradlePluginDevelopmentTestSuiteFactory {
    private final ObjectFactory objectFactory;

    DefaultGradlePluginDevelopmentTestSuiteFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public GradlePluginDevelopmentTestSuite create(String name) {
        return objectFactory.newInstance(GradlePluginDevelopmentTestSuiteInternal.class, name);
    }
}
