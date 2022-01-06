package dev.gradleplugins;

import dev.gradleplugins.internal.FinalizableComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FinalizableComponentTestUtils {
    public static <T> T finalizeComponent(T component) {
        assertTrue(component instanceof FinalizableComponent);
        ((FinalizableComponent) component).finalizeComponent();
        return component;
    }
}
