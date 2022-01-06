package dev.gradleplugins.internal.jvm;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.gradle.api.JavaVersion.VERSION_1_6;
import static org.gradle.api.JavaVersion.VERSION_1_9;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JvmTargetCompatibilityPropertyTest {
    private final JvmCompatibilityProperty delegate = Mockito.mock(JvmCompatibilityProperty.class);
    private final JvmTargetCompatibilityProperty subject = new JvmTargetCompatibilityProperty(delegate);

    @Test
    void throwsNullPointerExceptionWhenSetValueIsNull() {
        assertThrows(NullPointerException.class, () -> subject.set(null));
    }

    @Test
    void forwardsGetToDelegate() {
        when(delegate.get()).thenReturn(VERSION_1_9);

        assertSame(VERSION_1_9, subject.get());
        verify(delegate).get();
    }

    @Test
    void forwardsSetToDelegate() {
        subject.set(VERSION_1_6);
        verify(delegate).set(VERSION_1_6);
    }

    @Test
    void forwardsFinalizeValueToDelegate() {
        subject.finalizeValue();
        verify(delegate).finalizeValue();
    }
}
