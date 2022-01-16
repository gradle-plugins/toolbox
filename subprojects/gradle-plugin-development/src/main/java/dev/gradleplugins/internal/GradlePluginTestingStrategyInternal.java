package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;

public interface GradlePluginTestingStrategyInternal extends GradlePluginTestingStrategy {
    String MINIMUM_GRADLE = "minimumGradle";
    String LATEST_NIGHTLY = "latestNightly";
    String LATEST_GLOBAL_AVAILABLE = "latestGlobalAvailable";
}
