package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import org.gradle.api.Named;

public interface GradlePluginTestingStrategyInternal extends GradlePluginTestingStrategy, Named {
    String MINIMUM_GRADLE = "minimumGradle";
    String LATEST_NIGHTLY = "latestNightly";
    String LATEST_GLOBAL_AVAILABLE = "latestGlobalAvailable";
}
