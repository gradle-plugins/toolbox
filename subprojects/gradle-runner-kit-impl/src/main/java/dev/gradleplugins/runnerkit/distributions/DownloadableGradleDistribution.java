package dev.gradleplugins.runnerkit.distributions;

import dev.gradleplugins.runnerkit.GradleDistribution;

import java.net.URI;

public interface DownloadableGradleDistribution extends GradleDistribution {
    URI getUri();
}
