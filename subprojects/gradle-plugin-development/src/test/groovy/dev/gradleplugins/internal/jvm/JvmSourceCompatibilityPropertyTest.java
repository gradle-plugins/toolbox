package dev.gradleplugins.internal.jvm;

import org.gradle.api.JavaVersion;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.gradle.api.JavaVersion.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JvmSourceCompatibilityPropertyTest {
    private final JvmCompatibilityProperty delegate = Mockito.mock(JvmCompatibilityProperty.class);
    private final JvmSourceCompatibilityProperty subject = new JvmSourceCompatibilityProperty(delegate);

    @Test
    void throwsNullPointerExceptionWhenSetValueIsNull() {
        assertThrows(NullPointerException.class, () -> subject.set(null));
    }

    @Test
    void forwardsGetToDelegate() {
        when(delegate.get()).thenReturn(VERSION_1_8);

        assertSame(VERSION_1_8, subject.get());
        verify(delegate).get();
    }

    @Test
    void forwardsSetToDelegate() {
        subject.set(VERSION_1_5);
        verify(delegate).set(VERSION_1_5);
    }

    @Test
    void forwardsFinalizeValueToDelegate() {
        subject.finalizeValue();
        verify(delegate).finalizeValue();
    }

    @Test
    void disallowChangesWhenFinalized() {
        subject.finalizeValue();
        final Throwable ex = assertThrows(IllegalStateException.class, () -> subject.set(VERSION_12));
        assertEquals("The value for property 'sourceCompatibility' is final and cannot be changed any further.", ex.getMessage());
    }

    @Test
    void doesNotForwardsFinalizeValueToDelegateOnSubsequentFinalize() {
        subject.finalizeValue();
        subject.finalizeValue();
        verify(delegate).finalizeValue();
    }
}
