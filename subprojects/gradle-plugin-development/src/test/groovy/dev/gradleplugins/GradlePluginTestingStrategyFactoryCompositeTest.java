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
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradlePluginTestingStrategyFactoryCompositeTest {
    private final ProviderFactory providerFactory = ProjectBuilder.builder().build().getProviders();
    private final GradlePluginTestingStrategyFactory factory = new GradlePluginTestingStrategyFactoryInternal(providerFactory.provider(() -> null), new ReleasedVersionDistributions(empty()));

    @Test
    void throwsExceptionWhenComposingStrategiesOfSameType() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.composite(anotherStrategy(), anotherStrategy("jilo")));
        assertEquals("Unable to compose testing strategy with multiple AnotherStrategy type.", ex.getMessage());
    }

    @Test
    void throwsExceptionWhenComposingSameStrategies() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.composite(aStrategy(), aStrategy()));
        assertEquals("Unable to compose testing strategy with multiple aStrategy() instances.", ex.getMessage());
    }

    @Test
    void throwsExceptionWhenFirstStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> factory.composite(null, aStrategy(), anotherStrategy()));
    }

    @Test
    void throwsExceptionWhenSecondStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> factory.composite(aStrategy(), null, anotherStrategy()));
    }

    @Test
    void throwsExceptionWhenAnotherStrategyIsNull() {
        assertThrows(NullPointerException.class, () -> factory.composite(aStrategy(), anotherStrategy(), () -> "dekw", null));
    }

    @Test
    void throwsExceptionWhenComposingCompositeStrategies() {
        final Throwable ex = assertThrows(IllegalArgumentException.class, () -> factory.composite(factory.composite(aStrategy(), anotherStrategy("first")), anotherStrategy("second")));
        assertEquals("Unable to compose testing strategy from composite testing strategies.", ex.getMessage());
    }
}
