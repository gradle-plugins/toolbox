package dev.gradleplugins.fixtures.junit4.internal;

import org.junit.rules.ExternalResource;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class AutoCloseableExternalResource extends ExternalResource {
    private final AutoCloseable delegate;

    public AutoCloseableExternalResource(AutoCloseable delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void after() {
        try {
            delegate.close();
        } catch (Exception e) {
            rethrow(e);
        }
    }
}
