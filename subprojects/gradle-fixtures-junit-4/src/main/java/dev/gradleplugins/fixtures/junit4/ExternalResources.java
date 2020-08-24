package dev.gradleplugins.fixtures.junit4;

import dev.gradleplugins.fixtures.junit4.internal.AutoCloseableExternalResource;
import dev.gradleplugins.fixtures.junit4.internal.SupplierExternalResource;
import org.junit.rules.ExternalResource;

import java.util.function.Supplier;

public class ExternalResources {
    public static ExternalResource of(AutoCloseable closeable) {
        return new AutoCloseableExternalResource(closeable);
    }

    public static <T> ExternalResource fromSupplier(Supplier<T> supplier) {
        return new SupplierExternalResource<>(supplier);
    }
}
