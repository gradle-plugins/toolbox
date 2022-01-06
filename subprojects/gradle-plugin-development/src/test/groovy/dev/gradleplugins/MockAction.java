package dev.gradleplugins;

import org.gradle.api.Action;

public final class MockAction<T> implements Action<T> {
    private T argument = null;
    private boolean actionCalled = false;
    @Override
    public void execute(T t) {
        this.argument = t;
        actionCalled = true;
    }

    public T getArgument() {
        return argument;
    }

    public boolean isActionCalled() {
        return actionCalled;
    }
}
