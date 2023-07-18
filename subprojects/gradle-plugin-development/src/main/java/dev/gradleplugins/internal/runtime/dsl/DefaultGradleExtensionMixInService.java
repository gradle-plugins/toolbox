package dev.gradleplugins.internal.runtime.dsl;

import org.gradle.api.model.ObjectFactory;

public final class DefaultGradleExtensionMixInService implements GradleExtensionMixInService {
    private final ObjectFactory objects;

    public DefaultGradleExtensionMixInService(ObjectFactory objects) {
        this.objects = objects;
    }

    @Override
    public <T> GradleExtensionMixInBuilder<T> forExtension(Class<T> extensionType) {
        return new DefaultGradleExtensionMixInBuilder<>(objects, extensionType);
    }
}
