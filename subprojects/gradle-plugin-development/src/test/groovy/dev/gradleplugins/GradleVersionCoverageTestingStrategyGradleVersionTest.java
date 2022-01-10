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

class GradleVersionCoverageTestingStrategyGradleVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "6.7"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("6.6"))
            .version(globalAvailable("6.7"))
            .version(globalAvailable("6.8"))
            .version(globalAvailable("6.9"))
            .version(globalAvailable("7.0"))
            .version(current(globalAvailable("7.1")))
            .build()));
    private final GradleVersionCoverageTestingStrategy subject = factory.coverageForGradleVersion("6.8");

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
        assertEquals("6.8", subject.getVersion());
    }

    @Test
    void usesVersionAsName() {
        assertThat(subject, named("6.8"));
    }

    @Test
    void hasToString() {
        assertThat(subject, Matchers.hasToString("coverage for Gradle v6.8"));
    }
}
