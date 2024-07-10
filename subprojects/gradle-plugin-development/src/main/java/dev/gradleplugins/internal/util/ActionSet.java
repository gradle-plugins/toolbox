package dev.gradleplugins.internal.util;

import org.gradle.api.Action;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ActionSet<T> implements Action<T> {
    private final Set<Action<? super T>> actions = new LinkedHashSet<>();

    public boolean add(Action<? super T> action) {
        return actions.add(action);
    }

    @Override
    public void execute(T t) {
        for (Action<? super T> action : actions) {
            action.execute(t);
        }
    }
}
