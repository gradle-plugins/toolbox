package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class GradlePluginTestingStrategyFactoryNoMinimumVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory subject = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> null), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("7.3"))
            .version(snapshotFor("7.4"))
            .version(releaseCandidateFor("7.4"))
            .version(current(globalAvailable("7.4")))
            .version(snapshotFor("7.5"))
            .build()));

    @Test
    void throwsExceptionOnMinimumVersionQuery() {
        assertThrows(IllegalStateException.class, () -> subject.getCoverageForMinimumVersion().getVersion());
    }

    @Test
    void returnsAbsentProviderOnLatestGlobalAvailableVersionOfEachMajorVersion() {
        assertThat(subject.getCoverageForLatestGlobalAvailableVersionOfEachMajorVersion(), absentProvider());
    }

    @Test
    void doesNotThrowExceptionWhenLatestGlobalAvailableVersionQuery() {
        assertDoesNotThrow(() -> subject.getCoverageForLatestGlobalAvailableVersion().getVersion());
    }

    @Test
    void doesNotThrowExceptionWhenLatestNightlyVersionQuery() {
        assertDoesNotThrow(() -> subject.getCoverageForLatestNightlyVersion().getVersion());
    }

    @Test
    void doesNotThrowExceptionWhenGradleVersionQuery() {
        assertDoesNotThrow(() -> subject.coverageForGradleVersion("7.3").getVersion());
    }
}
