package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.current;
import static dev.gradleplugins.GradleReleases.globalAvailable;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleVersionCoverageTestingStrategyMinimumVersionAsLatestGlobalAvailableVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private ReleasedVersionDistributions.GradleRelease latestGlobalAvailable;
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> latestGlobalAvailable.getVersion()), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("6.0"))
            .version(globalAvailable("6.1"))
            .version(latestGlobalAvailable = current(globalAvailable("6.2")))
            .build()));

    @Test
    void isLatestGlobalAvailable() {
        assertTrue(factory.getCoverageForMinimumVersion().isLatestGlobalAvailable());
    }

    @Test
    void equalsToLatestGlobalAvailableVersion() {
        assertEquals(factory.getCoverageForLatestGlobalAvailableVersion().getVersion(), factory.getCoverageForMinimumVersion().getVersion());
    }
}
