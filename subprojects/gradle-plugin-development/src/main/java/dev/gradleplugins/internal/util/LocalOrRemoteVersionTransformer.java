package dev.gradleplugins.internal.util;

import org.gradle.api.Transformer;

import java.util.function.Function;
import java.util.function.Supplier;

public final class LocalOrRemoteVersionTransformer<T> implements Transformer<T, String> {
    private static final String LOCAL_GRADLE_VERSION = "local";
    private final Supplier<T> localSupplier;
    private final Function<String, T> remoteMapper;

    public LocalOrRemoteVersionTransformer(Supplier<T> localSupplier, Function<String, T> remoteMapper) {
        this.localSupplier = localSupplier;
        this.remoteMapper = remoteMapper;
    }

    @Override
    public T transform(String version) {
        if (LOCAL_GRADLE_VERSION.equals(version)) {
            return localSupplier.get();
        }
        return remoteMapper.apply(version);
    }
}
