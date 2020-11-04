package dev.gradleplugins.runnerkit.distributions;

import dev.gradleplugins.runnerkit.GradleDistribution;

public interface VersionAwareGradleDistribution extends GradleDistribution {
    String getVersion();
}
