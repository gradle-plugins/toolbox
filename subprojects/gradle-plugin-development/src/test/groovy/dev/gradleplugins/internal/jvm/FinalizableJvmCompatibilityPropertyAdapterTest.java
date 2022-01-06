package dev.gradleplugins.internal.jvm;

import org.junit.jupiter.api.Test;

import static org.gradle.api.JavaVersion.*;
import static org.junit.jupiter.api.Assertions.*;

class FinalizableJvmCompatibilityPropertyAdapterTest {
    private final MockJvmCompatibilityProperty delegate = new MockJvmCompatibilityProperty(VERSION_1_8);
    private final FinalizableJvmCompatibilityPropertyAdapter subject = new FinalizableJvmCompatibilityPropertyAdapter(delegate);

    @Test
    void setsDelegateToJavaVersion1_1() {
        assertEquals(VERSION_1_1, delegate.value);
    }

    @Test
    void throwsNullPointerExceptionWhenSetValueIsNull() {
        assertThrows(NullPointerException.class, () -> subject.set(null));
    }

    @Test
    void forwardsGetToDelegate() {
        delegate.set(VERSION_1_7);
        assertSame(VERSION_1_7, subject.get());
    }

    @Test
    void forwardsSetToDelegateOnlyWhenValueIsJavaVersion1_1() {
        delegate.value = VERSION_1_1;
        subject.set(VERSION_1_5);
        assertEquals(VERSION_1_5, delegate.value);
    }

    @Test
    void doesNotForwardSetToDelegateWhenValueIsNotJavaVersion1_1() {
        delegate.value = VERSION_1_4;
        subject.set(VERSION_1_9);
        assertEquals(VERSION_1_4, delegate.value);
    }

    @Test
    void restoresInitialValueToDelegateWhenValueIsJavaVersion1_1OnFinalizeValue() {
        delegate.value = VERSION_1_1;
        subject.finalizeValue();
        assertEquals(VERSION_1_8, delegate.value);
    }

    @Test
    void doesNotRestoresInitialValueToDelegateWhenValueIsJavaVersion1_1OnFinalizeValue() {
        delegate.value = VERSION_11;
        subject.finalizeValue();
        assertEquals(VERSION_11, delegate.value);
    }
}
