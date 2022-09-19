package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.specs.Spec;

public final class DomainObjectCollectionUtils {
    private DomainObjectCollectionUtils() {}

    public static <S> Action<S> matching(Spec<? super S> spec, Action<? super S> action) {
        return it -> {
            if (spec.isSatisfiedBy(it)) {
                action.execute(it);
            }
        };
    }
}
