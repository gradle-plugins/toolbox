package dev.gradleplugins.internal.runtime.dsl;

import org.gradle.api.model.ObjectFactory;

final class DefaultGradleExtensionMixInBuilder<T> implements GradleExtensionMixInBuilder<T> {
    private final ObjectFactory objects;
    private final Class<T> publicType;
    private T instance;

    public DefaultGradleExtensionMixInBuilder(ObjectFactory objects, Class<T> publicType) {
        this.objects = objects;
        this.publicType = publicType;
    }

    @Override
    public GradleExtensionMixInBuilder<T> useInstance(T instance) {
        this.instance = instance;
        return this;
    }

    @Override
    public GradleExtensionMixIn build() {
        if (instance == null) {
            instance = objects.newInstance(publicType);
        }

        return new DefaultGradleExtensionMixIn<>(publicType, instance);
    }
}
