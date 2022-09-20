package dev.gradleplugins.internal.util;

import org.gradle.api.Transformer;
import org.gradle.api.provider.HasConfigurableValue;
import org.gradle.api.provider.Provider;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class ProviderUtils {
    private ProviderUtils() {}

    @SuppressWarnings("UnstableApiUsage")
    public static <S extends HasConfigurableValue> S disallowChanges(S self) {
        self.disallowChanges();
        return self;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <S extends HasConfigurableValue> S finalizeValue(S self) {
        self.finalizeValue();
        return self;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <S extends HasConfigurableValue> S finalizeValueOnRead(S self) {
        self.finalizeValueOnRead();
        return self;
    }

    public static <S> void ifPresent(Provider<S> self, Consumer<? super S> action) {
        final S value = self.getOrNull();
        if (value != null) {
            action.accept(value);
        }
    }

    public static <OUT, IN, ITERABLE extends Iterable<? extends IN>> Transformer<Iterable<OUT>, ITERABLE> transformEach(Transformer<? extends OUT, ? super IN> transformer) {
        return it -> StreamSupport.stream(it.spliterator(), false).map(transformer::transform).collect(Collectors.toList());
    }
}
