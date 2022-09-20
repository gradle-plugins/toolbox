package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.Collections;

public final class DomainObjectCollectionUtils {
    private DomainObjectCollectionUtils() {}

    public static <S> Action<S> matching(Spec<? super S> spec, Action<? super S> action) {
        return it -> {
            if (spec.isSatisfiedBy(it)) {
                action.execute(it);
            }
        };
    }

    /**
     * Helper methods around {@link org.gradle.api.DomainObjectCollection#addAllLater} for optional element.
     *
     * @param self  the optional element provider to add, must not be null
     * @return an single-element or empty iterable provider, never null
     * @param <ELEMENT>  the element type
     */
    public static <ELEMENT> Provider<? extends Iterable<ELEMENT>> singleOrEmpty(Provider<ELEMENT> self) {
        return self.map(Collections::singletonList).orElse(Collections.emptyList());
    }
}
