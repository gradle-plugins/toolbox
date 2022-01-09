package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.ProjectMatchers.*;
import static dev.gradleplugins.internal.util.TestingStrategyPropertyUtils.testingStrategy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

class GradlePluginDevelopmentTestSuiteTestingStrategyPropertyOnTestTaskIntegrationTest {
    private static final TypeOf<Property<GradlePluginTestingStrategy>> PROPERTY_TYPE = new TypeOf<Property<GradlePluginTestingStrategy>>() {};
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = GradlePluginDevelopmentTestSuiteFactory.forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("boat");

    @BeforeEach
    void setup() {
        project.getPluginManager().apply("java-base");
        subject.getTestingStrategies().addAll(subject.getStrategies().coverageForGradleVersion("6.5"), subject.getStrategies().coverageForGradleVersion("6.6"), subject.getStrategies().coverageForGradleVersion("6.9"));
        subject.finalizeComponent();
    }

    @Test
    void hasTestingStrategyPropertyExtensionOnEachTestSuiteTestTask() {
        assertThat(project.getTasks().getByName("boat6.5"),
                extensions(hasItem(allOf(named("testingStrategy"), publicType(PROPERTY_TYPE)))));
        assertThat(project.getTasks().getByName("boat6.6"),
                extensions(hasItem(allOf(named("testingStrategy"), publicType(PROPERTY_TYPE)))));
        assertThat(project.getTasks().getByName("boat6.9"),
                extensions(hasItem(allOf(named("testingStrategy"), publicType(PROPERTY_TYPE)))));
    }

    @Test
    void hasCorrespondingTestingStrategyPropertyValue() {
        assertThat(testingStrategy(project.getTasks().getByName("boat6.5")),
                providerOf(subject.getStrategies().coverageForGradleVersion("6.5")));
        assertThat(testingStrategy(project.getTasks().getByName("boat6.6")),
                providerOf(subject.getStrategies().coverageForGradleVersion("6.6")));
        assertThat(testingStrategy(project.getTasks().getByName("boat6.9")),
                providerOf(subject.getStrategies().coverageForGradleVersion("6.9")));
    }
}
