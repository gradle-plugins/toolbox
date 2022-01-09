package dev.gradleplugins;

import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory.forProject;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GradlePluginDevelopmentTestSuiteIntegrationTest implements GradlePluginDevelopmentTestSuiteTester {
    private final Project project = ProjectBuilder.builder().build();
    private final GradlePluginDevelopmentTestSuiteFactory factory = forProject(project);
    private final GradlePluginDevelopmentTestSuite subject = factory.create("gote");

    @Override
    public GradlePluginDevelopmentTestSuite subject() {
        return subject;
    }

    @Test
    void finalizeSourceSetPropertyOnRead() {
        subject.getSourceSet().value(mock(SourceSet.class)).get();
        assertThrows(RuntimeException.class, () -> subject.getSourceSet().set(mock(SourceSet.class)));
    }

    @Test
    void finalizeTestingStrategiesPropertyOnRead() {
        subject.getTestingStrategies().value(singleton(mock(GradlePluginTestingStrategy.class))).get();
        assertThrows(RuntimeException.class, () -> subject.getTestingStrategies().set(singleton(mock(GradlePluginTestingStrategy.class))));
    }

    @Test
    void throwsExceptionOnSourceSetPropertyQueryWhenJavaBasePluginNotApplied() {
        Throwable ex = assertThrows(RuntimeException.class, () -> subject.getSourceSet().get());
        assertEquals("Please apply 'java-base' plugin.", ex.getMessage());
    }

    @Test
    void hasToString() {
        assertThat(subject(), Matchers.hasToString("test suite 'gote'"));
    }
}
