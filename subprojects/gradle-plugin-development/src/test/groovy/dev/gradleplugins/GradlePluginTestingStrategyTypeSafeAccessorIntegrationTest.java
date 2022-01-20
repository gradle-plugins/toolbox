package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.provider.Property;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;
import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GradlePluginTestingStrategyTypeSafeAccessorIntegrationTest {
    private final Project project = ProjectBuilder.builder().build();
    private final org.gradle.api.tasks.testing.Test subject = project.getTasks().create("jefe", org.gradle.api.tasks.testing.Test.class);

    @Test
    void throwsExceptionWhenExtensionNotPresent() {
        assertThrows(UnknownDomainObjectException.class, () -> testingStrategy(subject));
    }

    @Test
    void returnsLiveProviderWhenExtensionIsPresent() {
        final Property<GradlePluginTestingStrategy> property = project.getObjects().property(GradlePluginTestingStrategy.class);
        subject.getExtensions().add("testingStrategy", property);

        assertThat(testingStrategy(subject), absentProvider());

        final GradlePluginTestingStrategy value = mock(GradlePluginTestingStrategy.class);
        property.set(value);
        assertThat(testingStrategy(subject), providerOf(value));
    }
}
