package dev.gradleplugins.internal.util;

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static dev.gradleplugins.internal.util.ProviderUtils.ifPresent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ProviderUtils_IfPresentTests {
    @Mock Consumer<Object> presentAction;

    @Test
    void callsActionIfProviderValueIsPresent() {
        final Object value = new Object();
        ifPresent(providerFactory().provider(() -> value), presentAction);
        Mockito.verify(presentAction).accept(value);
    }

    @Test
    void doesNoCallActionIfProviderValueIsAbsent() {
        ifPresent(providerFactory().provider(() -> null), presentAction);
        Mockito.verify(presentAction, never()).accept(any());
    }

    public static ProviderFactory providerFactory() {
        return ProjectBuilder.builder().build().getProviders();
    }
}
