package dev.gradleplugins.internal.util;

import org.gradle.api.tasks.SourceSet;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class SourceSetUtils_CompileOnlyApiConfigurationNameTests {
    private static final GradleVersion BELOW_6_7 = GradleVersion.version("6.5");
    private static final GradleVersion ABOVE_6_7 = GradleVersion.version("6.9");
    @Mock SourceSet subject;

    @ParameterizedTest
    @ValueSource(strings = {"6.7", "6.7.1", "6.9", "7.0"})
    void returnsCompileOnlyApiConfigurationNameOnGradleVersionAbove6_7(String version) {
        GradleTestUtils.setCurrentGradleVersion(GradleVersion.version(version));
        Mockito.when(subject.getCompileOnlyConfigurationName()).thenReturn("compileOnly");
        assertThat(SourceSetUtils.compileOnlyApiConfigurationName(subject), equalTo("compileOnlyApi"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"6.6.1", "6.5", "6.2.1"})
    void returnsCompileOnlyConfigurationNameOnGradleVersionBelow6_7(String version) {
        GradleTestUtils.setCurrentGradleVersion(GradleVersion.version(version));
        Mockito.when(subject.getCompileOnlyConfigurationName()).thenReturn("compileOnly");
        assertThat(SourceSetUtils.compileOnlyApiConfigurationName(subject), equalTo("compileOnly"));
    }

    @Test
    void includesSourceSetNameInCompileOnlyConfigurationNameForNonMainSourceSet() {
        GradleTestUtils.setCurrentGradleVersion(BELOW_6_7);
        Mockito.when(subject.getCompileOnlyConfigurationName()).thenReturn("fooCompileOnly");
        assertThat(SourceSetUtils.compileOnlyApiConfigurationName(subject), equalTo("fooCompileOnly"));
    }

    @Test
    void includesSourceSetNameInCompileOnlyApiConfigurationNameForNonMainSourceSet() {
        GradleTestUtils.setCurrentGradleVersion(ABOVE_6_7);
        Mockito.when(subject.getCompileOnlyConfigurationName()).thenReturn("fooCompileOnly");
        assertThat(SourceSetUtils.compileOnlyApiConfigurationName(subject), equalTo("fooCompileOnlyApi"));
    }
}
