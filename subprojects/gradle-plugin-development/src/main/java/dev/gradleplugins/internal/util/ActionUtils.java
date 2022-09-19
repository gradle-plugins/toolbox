package dev.gradleplugins.internal.util;

import org.gradle.api.Action;

public final class ActionUtils {
    private ActionUtils() {}

    public static <S> Action<S> withoutParameter(Runnable runnable) {
        return __ -> runnable.run();
    }
}
