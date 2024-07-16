package dev.gradleplugins.internal.util;

import org.gradle.api.Action;

public final class OnceAction<T> implements Action<T> {
    private final Runnable delegate;
    private boolean executed = false;

    private OnceAction(Runnable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(T t) {
        if (!executed) {
            executed = true;
            delegate.run();
        }
    }

    public static <T> OnceAction<T> once(Runnable action) {
        return new OnceAction<>(action);
    }
}
