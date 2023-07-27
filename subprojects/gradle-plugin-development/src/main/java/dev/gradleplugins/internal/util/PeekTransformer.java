package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.Transformer;

public final class PeekTransformer<T> implements Transformer<T, T> {
    private final Action<? super T> peekAction;

    public PeekTransformer(Action<? super T> peekAction) {
        this.peekAction = peekAction;
    }

    @Override
    public T transform(T t) {
        peekAction.execute(t);
        return t;
    }
}
