package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.util.GradleVersion;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GradleVersionCoverageTestingStrategyLatestNightlyVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private ReleasedVersionDistributions.GradleRelease latestSnapshot;
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "6.3"), new ReleasedVersionDistributions(builder()
            .version(snapshotFor("6.2"))
            .version(globalAvailable("6.2"))
            .version(snapshotFor("6.3"))
            .version(globalAvailable("6.3"))
            .version(snapshotFor("6.4"))
            .version(releaseCandidateFor("6.4"))
            .version(current(globalAvailable("6.4")))
            .version(latestSnapshot = snapshotFor("6.5"))
            .build()));
    private final GradleVersionCoverageTestingStrategy subject = factory.getCoverageForLatestNightlyVersion();

    @Test
    void isLatestNightly() {
        assertTrue(subject.isLatestNightly());
    }

    @Test
    void isNotLatestGlobalAvailable() {
        assertFalse(subject.isLatestGlobalAvailable());
    }

    @Test
    void hasVersion() {
        assertEquals(latestSnapshot.getVersion(), subject.getVersion());
    }

    @Test
    void usesConceptAsName() {
        assertThat(subject, named("latestNightly"));
    }

    @Test
    void hasToString() {
        assertThat(subject, Matchers.hasToString("coverage for Gradle v" + latestSnapshot.getVersion()));
    }
}
