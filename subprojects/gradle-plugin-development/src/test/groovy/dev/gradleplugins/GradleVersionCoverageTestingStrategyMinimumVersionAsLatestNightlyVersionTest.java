package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradleVersionCoverageTestingStrategyMinimumVersionAsLatestNightlyVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private ReleasedVersionDistributions.GradleRelease latestSnapshot;
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> latestSnapshot.getVersion()), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("7.0"))
            .version(globalAvailable("7.1"))
            .version(snapshotFor("7.2"))
            .version(snapshotFor("7.2"))
            .version(releaseCandidateFor("7.2"))
            .version(releaseCandidateFor("7.2"))
            .version(current(globalAvailable("7.2")))
            .version(latestSnapshot = snapshotFor("7.3"))
            .build()));

    @Test
    void isLatestNightly() {
        assertTrue(factory.getCoverageForMinimumVersion().isLatestNightly());
    }

    @Test
    void equalsToLatestNightlyVersion() {
        assertEquals(factory.getCoverageForLatestNightlyVersion().getVersion(), factory.getCoverageForMinimumVersion().getVersion());
    }
}
