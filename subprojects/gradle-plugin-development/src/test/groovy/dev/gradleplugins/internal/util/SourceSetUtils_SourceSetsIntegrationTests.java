package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.gradleplugins.ProjectMatchers.absentProvider;
import static dev.gradleplugins.ProjectMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class SourceSetUtils_SourceSetsIntegrationTests {
    Project project = ProjectBuilder.builder().build();
    @Mock Action<SourceSetContainer> action;

    @Nested
    class HasSourceSetContainerTest {
        @Mock SourceSetContainer sourceSets;

        @BeforeEach
        void configureProject() {
            project.getExtensions().add("sourceSets", sourceSets);
        }

        @Test
        void callsActionWithSourceSetContainer() {
            SourceSetUtils.sourceSets(project, action);
            Mockito.verify(action).execute(sourceSets);
        }

        @Test
        void returnsProviderOfSourceSetContainer() {
            assertThat(SourceSetUtils.sourceSets(project), providerOf(sourceSets));
        }
    }

    @Test
    void returnsAbsentProviderWhenNoSourceSetContainerExtension() {
        assertThat(SourceSetUtils.sourceSets(project), absentProvider());
    }
}
