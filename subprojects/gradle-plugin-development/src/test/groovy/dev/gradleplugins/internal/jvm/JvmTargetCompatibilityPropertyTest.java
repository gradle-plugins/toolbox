package dev.gradleplugins.internal.jvm;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.gradle.api.JavaVersion.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    void disallowChangesWhenFinalized() {
        subject.finalizeValue();
        final Throwable ex = assertThrows(IllegalStateException.class, () -> subject.set(VERSION_11));
        assertEquals("The value for property 'targetCompatibility' is final and cannot be changed any further.", ex.getMessage());
    }

    @Test
    void doesNotForwardsFinalizeValueToDelegateOnSubsequentFinalize() {
        subject.finalizeValue();
        subject.finalizeValue();
        verify(delegate).finalizeValue();
    }
}
