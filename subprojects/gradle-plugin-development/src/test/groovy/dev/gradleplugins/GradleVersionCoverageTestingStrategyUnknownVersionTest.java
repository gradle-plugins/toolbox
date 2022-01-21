package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradleVersionCoverageTestingStrategyUnknownVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "2.5"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("3.0"))
            .version(snapshotFor("3.1"))
            .version(snapshotFor("3.1"))
            .version(releaseCandidateFor("3.1"))
            .version(globalAvailable("3.1"))
            .version(snapshotFor("3.2"))
            .version(releaseCandidateFor("3.2"))
            .version(current(globalAvailable("3.2")))
            .version(snapshotFor("3.3"))
            .build()));

    @Test
    void throwsExceptionWhenCoverageVersionResolvedIsUnknownGlobalAvailable() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.coverageForGradleVersion("5.4").getVersion());
        assertEquals("Unknown Gradle version '5.4' for adhoc testing strategy.", ex.getMessage());
    }

    @Test
    void throwsExceptionWhenCoverageVersionResolvedIsUnknownReleaseCandidate() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.coverageForGradleVersion("5.6-rc-1").getVersion());
        assertEquals("Unknown Gradle version '5.6-rc-1' for adhoc testing strategy.", ex.getMessage());
    }

    @Test
    void throwsExceptionWhenCoverageVersionResolvedIsUnknownSnapshot() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.coverageForGradleVersion("3.2-20220109234542-0000").getVersion());
        assertEquals("Unknown Gradle version '3.2-20220109234542-0000' for adhoc testing strategy.", ex.getMessage());
    }

    @Test
    void throwsExceptionWhenMinimumVersionIsUnknown() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.getCoverageForMinimumVersion().getVersion());
        assertEquals("Unknown minimum Gradle version '2.5' for testing strategy.", ex.getMessage());
    }

    @Test
    void throwsExceptionWhenCoverageForLatestMinorOfEachMajorWithUnknownMinimumVersion() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.getCoverageForLatestGlobalAvailableVersionOfEachSupportedMajorVersions().get());
        assertEquals("Unknown minimum Gradle version '2.5' for testing strategy.", ex.getMessage());
    }
}
