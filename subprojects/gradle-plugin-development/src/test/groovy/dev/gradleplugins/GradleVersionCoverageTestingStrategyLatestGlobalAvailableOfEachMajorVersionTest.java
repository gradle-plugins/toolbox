package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static dev.gradleplugins.GradleReleases.current;
import static dev.gradleplugins.GradleReleases.globalAvailable;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class GradleVersionCoverageTestingStrategyLatestGlobalAvailableOfEachMajorVersionTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> "5.6"), new ReleasedVersionDistributions(builder()
            .version(globalAvailable("4.8"))
            .version(globalAvailable("4.9"))
            .version(globalAvailable("5.5"))
            .version(globalAvailable("5.6"))
            .version(globalAvailable("6.8"))
            .version(globalAvailable("6.9"))
            .version(globalAvailable("6.9.1"))
            .version(globalAvailable("6.9.2"))
            .version(globalAvailable("7.0"))
            .version(globalAvailable("7.1"))
            .version(current(globalAvailable("7.2")))
            .build()));
    private final Provider<Set<GradleVersionCoverageTestingStrategy>> subject = factory.getCoverageForLatestGlobalAvailableVersionOfEachMajorVersion();

    @Test
    void doesNotContainsLatestMinorOfMajorVersionPriorToMinimumVersion() {
        assertThat(subject, providerOf(not(hasItem(factory.coverageForGradleVersion("4.9")))));
    }

    @Test
    void hasLatestMinorOfEachMajorVersionsAfterMinimumVersionSortedByMajorVersions() {
        assertThat(subject, providerOf(contains(factory.coverageForGradleVersion("5.6"), factory.coverageForGradleVersion("6.9.2"), factory.coverageForGradleVersion("7.2"))));
    }

    @Test
    void includesLatestGlobalAvailable() {
        assertThat(subject, providerOf(hasItem(latestGlobalAvailable())));
    }

    @Test
    void usesVersionAsName() {
        assertThat(subject, providerOf(contains(named("5.6"), named("6.9.2"), named("7.2"))));
    }

    private static Matcher<GradleVersionCoverageTestingStrategy> latestGlobalAvailable() {
        return new TypeSafeMatcher<GradleVersionCoverageTestingStrategy>() {
            @Override
            protected boolean matchesSafely(GradleVersionCoverageTestingStrategy item) {
                return item.isLatestGlobalAvailable();
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }
}
