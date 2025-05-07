package dev.gradleplugins.internal.util;

import org.gradle.api.Action;

public final class DoNothingAction<T> implements Action<T> {
    private DoNothingAction() {}

    @Override
    public void execute(T t) {
        // do nothing
    }

    public static <T> Action<T> doNothing() {
        return new DoNothingAction<>();
    }
}
