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

class GradleVersionCoverageTestingStrategyMinimumVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "5.4"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("5.4"))
            .version(globalAvailable("5.5"))
            .version(current(globalAvailable("5.6")))
            .build()));
    private final GradleVersionCoverageTestingStrategy subject = factory.getCoverageForMinimumVersion();

    @Test
    void isNotLatestNightly() {
        assertFalse(subject.isLatestNightly());
    }

    @Test
    void isNotLatestGlobalAvailable() {
        assertFalse(subject.isLatestGlobalAvailable());
    }

    @Test
    void hasMinimumVersion() {
        assertEquals("5.4", subject.getVersion());
    }

    @Test
    void usesConceptAsName() {
        assertThat(subject, named("minimumGradle"));
    }

    @Test
    void hasToString() {
        assertThat(subject, Matchers.hasToString("coverage for Gradle v5.4"));
    }
}
