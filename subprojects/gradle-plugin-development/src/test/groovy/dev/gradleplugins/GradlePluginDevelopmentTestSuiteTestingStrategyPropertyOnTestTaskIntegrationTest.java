package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;
import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static dev.gradleplugins.ProjectMatchers.extensions;
import static dev.gradleplugins.ProjectMatchers.named;
import static dev.gradleplugins.ProjectMatchers.presentProvider;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static dev.gradleplugins.ProjectMatchers.publicType;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GradlePluginDevelopmentTestSuiteTestingStrategyPropertyOnTestTaskIntegrationTest {
    private static final TypeOf<Property<GradlePluginTestingStrategy>> PROPERTY_TYPE = new TypeOf<Property<GradlePluginTestingStrategy>>() {};
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory factory = new DefaultGradlePluginDevelopmentTestSuiteFactory(project);
    GradlePluginDevelopmentTestSuite subject = factory.create("boat");

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("java-base");
        subject.getTestingStrategies().addAll(subject.getStrategies().coverageForGradleVersion("6.5"), subject.getStrategies().coverageForGradleVersion("6.6"), subject.getStrategies().coverageForGradleVersion("6.9"));
    }

    @Test
    void hasTestingStrategyPropertyExtensionOnEachTestSuiteTestTask() {
        assertThat(subject.getTestTasks().getElements(),
                providerOf(hasItem(allOf(named("boat6.5"), hasTestingStrategyProperty()))));
        assertThat(subject.getTestTasks().getElements(),
                providerOf(hasItem(allOf(named("boat6.6"), hasTestingStrategyProperty()))));
        assertThat(subject.getTestTasks().getElements(),
                providerOf(hasItem(allOf(named("boat6.9"), hasTestingStrategyProperty()))));
    }

    private static <T> Matcher<T> hasTestingStrategyProperty() {
        return extensions(hasItem(allOf(named("testingStrategy"), publicType(PROPERTY_TYPE))));
    }

    @Test
    void hasCorrespondingTestingStrategyPropertyValue() {
        assertThat(subject.getTestTasks().getElements(),
                providerOf(hasItem(allOf(named("boat6.5"),
                        testingStrategyOf(subject.getStrategies().coverageForGradleVersion("6.5"))))));
        assertThat(subject.getTestTasks().getElements(),
                providerOf(hasItem(allOf(named("boat6.6"),
                        testingStrategyOf(subject.getStrategies().coverageForGradleVersion("6.6"))))));
        assertThat(subject.getTestTasks().getElements(),
                providerOf(hasItem(allOf(named("boat6.9"),
                        testingStrategyOf(subject.getStrategies().coverageForGradleVersion("6.9"))))));
    }

    private static Matcher<org.gradle.api.tasks.testing.Test> testingStrategyOf(GradlePluginTestingStrategy instance) {
        return new FeatureMatcher<org.gradle.api.tasks.testing.Test, GradlePluginTestingStrategy>(equalTo(instance), "", "") {
            @Override
            protected GradlePluginTestingStrategy featureValueOf(org.gradle.api.tasks.testing.Test actual) {
                return testingStrategy(actual).getOrNull();
            }
        };
    }

    @Test
    void canReadTestingStrategyFromConfigureEachActionWithMultipleStrategies() {
        MockAction<org.gradle.api.tasks.testing.Test> action = new MockAction<>(task -> assertThat(testingStrategy(task), presentProvider()));
        subject.getTestTasks().configureEach(action);
        assertDoesNotThrow(() -> subject.getTestTasks().getElements().get());
        assertEquals(3, action.getCalledCount());
    }

    @Test
    void canReadTestingStrategyFromConfigureEachActionWithSingleStrategy() {
        subject.getTestingStrategies().set(singletonList(subject.getStrategies().coverageForGradleVersion("6.7")));
        MockAction<org.gradle.api.tasks.testing.Test> action = new MockAction<>(task -> assertThat(testingStrategy(task), presentProvider()));
        subject.getTestTasks().configureEach(action);
        assertDoesNotThrow(() -> subject.getTestTasks().getElements().get());
        assertEquals(1, action.getCalledCount());
    }

    @Test
    void hasAbsentTestingStrategyFromConfigureEachActionWithoutStrategies() {
        subject.getTestingStrategies().empty();
        MockAction<org.gradle.api.tasks.testing.Test> action = new MockAction<>(task -> assertThat(testingStrategy(task), absentProvider()));
        subject.getTestTasks().configureEach(action);
        assertDoesNotThrow(() -> subject.getTestTasks().getElements().get());
        assertEquals(1, action.getCalledCount());
    }
}
