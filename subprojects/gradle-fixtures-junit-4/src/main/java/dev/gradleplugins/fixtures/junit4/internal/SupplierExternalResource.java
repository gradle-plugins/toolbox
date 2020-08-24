package dev.gradleplugins.fixtures.junit4.internal;

import org.junit.rules.ExternalResource;

import java.util.function.Supplier;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class SupplierExternalResource<T> extends ExternalResource {
    private Supplier<T> supplier;
    private T resource;

    public SupplierExternalResource(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    protected void before() throws Throwable {
        resource = supplier.get();
        supplier = null;
    }

    @Override
    protected void after() {
        try {
            if (resource instanceof AutoCloseable) {
                ((AutoCloseable) resource).close();
            }
        } catch (Exception e) {
            rethrow(e);
        }
    }
}
