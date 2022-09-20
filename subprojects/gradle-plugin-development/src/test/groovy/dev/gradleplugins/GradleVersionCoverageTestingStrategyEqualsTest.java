package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.current;
import static dev.gradleplugins.GradleReleases.globalAvailable;
import static dev.gradleplugins.GradleReleases.snapshotFor;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GradleVersionCoverageTestingStrategyEqualsTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private ReleasedVersionDistributions.GradleRelease latestSnapshot;
    private ReleasedVersionDistributions.GradleRelease latestGlobalAvailable;
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "7.1"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("7.1"))
            .version(globalAvailable("7.2"))
            .version(snapshotFor("7.3"))
            .version(latestGlobalAvailable = current(globalAvailable("7.3")))
            .version(latestSnapshot = snapshotFor("7.4"))
            .build()));

    @Test
    void minimumVersionsAreEquals() {
        assertEquals(factory.getCoverageForMinimumVersion(), factory.getCoverageForMinimumVersion());
    }

    @Test
    void minimumVersionIsNotEqualToExactGradleVersion() {
        assertNotEquals(factory.coverageForGradleVersion("7.1"), factory.getCoverageForMinimumVersion());
    }

    @Test
    void minimumVersionIsNotEqualToLatestGlobalAvailableVersion() {
        assertNotEquals(factory.getCoverageForLatestGlobalAvailableVersion(), factory.getCoverageForMinimumVersion());
    }

    @Test
    void minimumVersionIsNotEqualToLatestNightlyVersion() {
        assertNotEquals(factory.getCoverageForLatestNightlyVersion(), factory.getCoverageForMinimumVersion());
    }

    @Test
    void latestNightlyAreEquals() {
        assertEquals(factory.getCoverageForLatestNightlyVersion(), factory.getCoverageForLatestNightlyVersion());
    }

    @Test
    void latestGlobalAvailableAreEquals() {
        assertEquals(factory.getCoverageForLatestGlobalAvailableVersion(), factory.getCoverageForLatestGlobalAvailableVersion());
    }

    @Test
    void latestGlobalAvailableIsEqualToExactGradleVersion() {
        assertEquals(factory.coverageForGradleVersion(latestGlobalAvailable.getVersion()).getVersion(), factory.getCoverageForLatestGlobalAvailableVersion().getVersion());
    }

    @Test
    void latestNightlyIsEqualToExactGradleVersion() {
        assertEquals(factory.coverageForGradleVersion(latestSnapshot.getVersion()).getVersion(), factory.getCoverageForLatestNightlyVersion().getVersion());
    }
}
