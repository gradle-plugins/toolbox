package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.gradleplugins.internal.GradlePluginTestingStrategyInternal.*;

public abstract class GradlePluginTestingStrategyFactoryInternal implements GradlePluginTestingStrategyFactory {
    @Inject
    protected abstract ObjectFactory getObjects();

    @Override
    public GradlePluginTestingStrategy getCoverageForMinimumVersion() {
        return getObjects().newInstance(GradlePluginTestingStrategyInternal.class, MINIMUM_GRADLE);
    }

    @Override
    public GradlePluginTestingStrategy getCoverageForLatestNightlyVersion() {
        return getObjects().newInstance(GradlePluginTestingStrategyInternal.class, LATEST_NIGHTLY);
    }

    @Override
    public GradlePluginTestingStrategy getCoverageForLatestGlobalAvailableVersion() {
        return getObjects().newInstance(GradlePluginTestingStrategyInternal.class, LATEST_GLOBAL_AVAILABLE);
    }

    @Override
    public GradlePluginTestingStrategy coverageForGradleVersion(String version) {
        return getObjects().newInstance(GradlePluginTestingStrategyInternal.class, version);
    }
}
