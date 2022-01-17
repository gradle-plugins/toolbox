package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.aStrategy;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.anotherStrategy;
import static dev.gradleplugins.GradleReleases.*;
import static dev.gradleplugins.GradleVersionsTestService.builder;
import static dev.gradleplugins.GradleVersionsTestService.empty;
import static dev.gradleplugins.ProjectMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeGradlePluginTestingStrategyTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> null), new ReleasedVersionDistributions(empty()));
    private final CompositeGradlePluginTestingStrategy subject = factory.composite(aStrategy(), anotherStrategy());

    @Test
    void canIterateThroughAllStrategies() {
        assertThat(subject, contains(aStrategy(), anotherStrategy()));
    }

    @Test
    void composeNameFromStrategies() {
        assertThat(subject, named("aStrategyAnotherStrategy"));
    }

    @Test
    void hasToString() {
        assertEquals("strategy composed of <aStrategy(), anotherStrategy()>", subject.toString());
    }
}
