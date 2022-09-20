package dev.gradleplugins.internal.util;

import org.gradle.api.tasks.SourceSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class SourceSetUtils_IsMainTests {
    @Mock SourceSet subject;

    @Test
    void returnsTrueWhenSourceSetNameIsMain() {
        Mockito.when(subject.getName()).thenReturn("main");
        assertThat(SourceSetUtils.isMain(subject), is(true));
    }

    @Test
    void returnsFalseWhenSourceSetNameIsNotMain() {
        Mockito.when(subject.getName()).thenReturn("something-else");
        assertThat(SourceSetUtils.isMain(subject), is(false));
    }
}
