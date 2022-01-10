package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GradleVersionCoverageTestingStrategyGradleReleaseCandidateVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private ReleasedVersionDistributions.GradleRelease releaseCandidate;
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "6.7"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("6.7"))
            .version(releaseCandidate = releaseCandidateFor("6.8"))
            .version(current(globalAvailable("6.8")))
            .version(snapshotFor("6.9"))
            .build()));
    private final GradleVersionCoverageTestingStrategy subject = factory.coverageForGradleVersion(releaseCandidate.getVersion());

    @Test
    void isNotLatestNightly() {
        assertFalse(subject.isLatestNightly());
    }

    @Test
    void isNotLatestGlobalAvailable() {
        assertFalse(subject.isLatestGlobalAvailable());
    }

    @Test
    void hasVersion() {
        assertEquals(releaseCandidate.getVersion(), subject.getVersion());
    }

    @Test
    void usesVersionAsName() {
        assertThat(subject, named(releaseCandidate.getVersion()));
    }

    @Test
    void hasToString() {
        assertThat(subject, Matchers.hasToString("coverage for Gradle v" + releaseCandidate.getVersion()));
    }
}
