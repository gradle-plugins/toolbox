package dev.gradleplugins;

import org.gradle.api.Action;

import java.util.function.Consumer;

public final class MockAction<T> implements Action<T> {
    private T argument = null;
    private boolean actionCalled = false;
    private final Consumer<? super T> listener;
    private int calledCount = 0;

    public MockAction() {
        this(t -> {});
    }

    public MockAction(Consumer<? super T> listener) {
        this.listener = listener;
    }

    @Override
    public void execute(T t) {
        this.argument = t;
        actionCalled = true;
        calledCount++;
        listener.accept(t);
    }

    public T getArgument() {
        return argument;
    }

    public boolean isActionCalled() {
        return actionCalled;
    }

    public int getCalledCount() {
        return calledCount;
    }
}
