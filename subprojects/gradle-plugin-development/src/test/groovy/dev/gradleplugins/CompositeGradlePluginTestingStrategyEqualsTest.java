package dev.gradleplugins;

import dev.gradleplugins.internal.GradlePluginTestingStrategyFactoryInternal;
import dev.gradleplugins.internal.ReleasedVersionDistributions;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.aStrategy;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.anotherStrategy;
import static dev.gradleplugins.GradleVersionsTestService.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CompositeGradlePluginTestingStrategyEqualsTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> null), new ReleasedVersionDistributions(empty()));

    @Test
    void compositeTestingStrategyOfSameStrategiesAreEquals() {
        assertEquals(factory.composite(aStrategy(), anotherStrategy()), factory.composite(aStrategy(), anotherStrategy()));
    }

    @Test
    void compositeTestingStrategyOfDifferentStrategiesAreNotEquals() {
        assertNotEquals(factory.composite(aStrategy(), anotherStrategy()), factory.composite(aStrategy(), anotherStrategy("loke")));
    }

    @Test
    void compositeTestingStrategyOfDifferentStrategyTypesAreNotEquals() {
        assertNotEquals(factory.composite(aStrategy(), anotherStrategy()), factory.composite(anotherStrategy(), aStrategy()));
    }
}
