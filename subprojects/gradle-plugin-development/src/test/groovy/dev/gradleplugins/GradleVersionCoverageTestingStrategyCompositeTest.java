package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.aStrategy;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.anotherStrategy;
import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradleVersionCoverageTestingStrategyCompositeTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "7.1"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("7.1"))
            .version(globalAvailable("7.2"))
            .version(releaseCandidateFor("7.3"))
            .version(current(globalAvailable("7.3")))
            .version(snapshotFor("7.4"))
            .build()));

    @Test
    void canCompositeCoverageVersionWithOtherStrategiesType() {
        assertDoesNotThrow(() -> factory.composite(factory.coverageForGradleVersion("7.2"), aStrategy(), anotherStrategy()));
    }

    @Test
    void throwsExceptionWhenComposingLatestGradleAvailableWithLatestNightly() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.getCoverageForLatestGlobalAvailableVersion(), factory.getCoverageForLatestNightlyVersion()));
    }

    @Test
    void throwsExceptionWhenComposingLatestGradleAvailableWithMinimumGradle() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.getCoverageForLatestGlobalAvailableVersion(), factory.getCoverageForMinimumVersion()));
    }

    @Test
    void throwsExceptionWhenComposingLatestGradleAvailableWithCoverageVersion() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.getCoverageForLatestGlobalAvailableVersion(), factory.coverageForGradleVersion("7.2")));
    }

    @Test
    void throwsExceptionWhenComposingLatestNightlyWithMinimumGradle() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.getCoverageForLatestNightlyVersion(), factory.getCoverageForMinimumVersion()));
    }

    @Test
    void throwsExceptionWhenComposingLatestNightlyWithCoverageVersion() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.getCoverageForLatestNightlyVersion(), factory.coverageForGradleVersion("7.3")));
    }

    @Test
    void throwsExceptionWhenComposingMinimumGradleWithCoverageVersion() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.getCoverageForMinimumVersion(), factory.coverageForGradleVersion("7.2")));
    }

    @Test
    void throwsExceptionWhenComposingMultipleCoverageVersion() {
        assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.coverageForGradleVersion("7.2"), factory.coverageForGradleVersion("7.3")));
    }
}
