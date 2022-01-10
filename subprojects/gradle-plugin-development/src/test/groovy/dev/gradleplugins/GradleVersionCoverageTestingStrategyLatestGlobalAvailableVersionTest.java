package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.current;
import static dev.gradleplugins.GradleReleases.globalAvailable;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GradleVersionCoverageTestingStrategyLatestGlobalAvailableVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private ReleasedVersionDistributions.GradleRelease latestGlobalAvailable;
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "7.2"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("6.9"))
            .version(globalAvailable("7.0"))
            .version(globalAvailable("7.1"))
            .version(globalAvailable("7.2"))
            .version(latestGlobalAvailable = current(globalAvailable("7.3")))
            .build()));
    private final GradleVersionCoverageTestingStrategy subject = factory.getCoverageForLatestGlobalAvailableVersion();

    @Test
    void isNotLatestNightly() {
        assertFalse(subject.isLatestNightly());
    }

    @Test
    void isLatestGlobalAvailable() {
        assertTrue(subject.isLatestGlobalAvailable());
    }

    @Test
    void hasVersion() {
        assertEquals(latestGlobalAvailable.getVersion(), subject.getVersion());
    }

    @Test
    void usesConceptAsName() {
        assertThat(subject, named("latestGlobalAvailable"));
    }

    @Test
    void hasToString() {
        assertThat(subject, Matchers.hasToString("coverage for Gradle v" + latestGlobalAvailable.getVersion()));
    }
}
