package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Named;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_={@Inject})
public abstract class GradlePluginTestingStrategyInternal implements GradlePluginTestingStrategy, Named {
    public static final String MINIMUM_GRADLE = "minimumGradle";
    public static final String LATEST_NIGHTLY = "latestNightly";
    public static final String LATEST_GLOBAL_AVAILABLE = "latestGlobalAvailable";
    @Getter private final String name;
}
