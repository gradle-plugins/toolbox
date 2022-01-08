package dev.gradleplugins;

import dev.gradleplugins.internal.plugins.GradlePluginDevelopmentPlugin;
import org.gradle.api.invocation.Gradle;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class GradlePluginDevelopmentPluginInitScriptTest {
    private final GradlePluginDevelopmentPlugin subject = ProjectBuilder.builder().build().getObjects().newInstance(GradlePluginDevelopmentPlugin.class);

    @Test
    void throwsExceptionWhenPluginAppliedInInitScript() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> subject.apply(mock(Gradle.class)));
        assertEquals("Please apply 'dev.gradleplugins.gradle-plugin-development' plugin inside the settings.gradle[.kts] or build.gradle[.kts] script.", ex.getMessage());
    }
}
