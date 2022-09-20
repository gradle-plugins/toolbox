package dev.gradleplugins.internal.util;

import org.gradle.api.provider.HasConfigurableValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("UnstableApiUsage")
@ExtendWith(MockitoExtension.class)
class ProviderUtils_DisallowChangesTests {
    @Mock HasConfigurableValue value;

    @Test
    void callsDisallowChanges() {
        ProviderUtils.disallowChanges(value);
        Mockito.verify(value).disallowChanges();
    }

    @Test
    void returnsSameValue() {
        assertSame(value, ProviderUtils.disallowChanges(value));
    }
}
