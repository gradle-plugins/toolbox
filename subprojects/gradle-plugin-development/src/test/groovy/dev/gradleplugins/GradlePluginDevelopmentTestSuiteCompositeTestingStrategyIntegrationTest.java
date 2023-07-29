package dev.gradleplugins;

import dev.gradleplugins.internal.DefaultGradlePluginDevelopmentTestSuiteFactory;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static dev.gradleplugins.GradlePluginTestingStrategy.testingStrategy;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.aStrategy;
import static dev.gradleplugins.GradlePluginTestingStrategyTestUtils.anotherStrategy;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.iterableWithSize;

class GradlePluginDevelopmentTestSuiteCompositeTestingStrategyIntegrationTest {
    Project project = ProjectBuilder.builder().build();
    GradlePluginDevelopmentTestSuiteFactory factory = new DefaultGradlePluginDevelopmentTestSuiteFactory(project);
    GradlePluginDevelopmentTestSuite subject = factory.create("ldke");
    CompositeGradlePluginTestingStrategy firstStrategy = subject.getStrategies().composite(aStrategy(), anotherStrategy(), subject.getStrategies().coverageForGradleVersion("6.7"));
    CompositeGradlePluginTestingStrategy secondStrategy = subject.getStrategies().composite(aStrategy(), anotherStrategy("dege"), subject.getStrategies().coverageForGradleVersion("7.0"));

    @BeforeEach
    void configureTestingStrategies() {
        project.getPluginManager().apply("java-base");
        subject.getTestingStrategies().add(firstStrategy);
        subject.getTestingStrategies().add(secondStrategy);
    }

    @Test
    void hasMultipleTestTasks() {
        assertThat(subject.getTestTasks().getElements(), providerOf(iterableWithSize(2)));
    }

    @Test
    void usesCompositeStrategyOnEachTestTasks() {
        assertThat(subject.getTestTasks().getElements().map(transformEach(this::asTestingStrategy)), providerOf(contains(firstStrategy, secondStrategy)));
    }

    @Test
    void detectsComposedCoverageVersionOnEachTestTasks() {
        assertThat(subject.getTestTasks().getElements().map(transformEach(this::asSystemProperties)), providerOf(contains(hasEntry("dev.gradleplugins.defaultGradleVersion", "6.7"), hasEntry("dev.gradleplugins.defaultGradleVersion", "7.0"))));
    }

    private static <OUT, IN> Transformer<Iterable<OUT>, Iterable<IN>> transformEach(Function<? super IN, ? extends OUT> mapper) {
        return iter -> stream(iter.spliterator(), false).map(mapper).collect(toList());
    }

    private GradlePluginTestingStrategy asTestingStrategy(org.gradle.api.tasks.testing.Test task) {
        return testingStrategy(task).get();
    }

    private Map<String, Object> asSystemProperties(org.gradle.api.tasks.testing.Test task) {
        return task.getSystemProperties();
    }
}
