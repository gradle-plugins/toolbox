package dev.gradleplugins.runnerkit.distributions;

import dev.gradleplugins.runnerkit.GradleDistribution;

import java.io.File;

public interface LocalGradleDistribution extends GradleDistribution {
    File getInstallationDirectory();
}
