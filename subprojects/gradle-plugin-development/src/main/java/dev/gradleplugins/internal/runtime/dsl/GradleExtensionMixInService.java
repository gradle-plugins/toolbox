package dev.gradleplugins.internal.runtime.dsl;

public interface GradleExtensionMixInService {
    <T> GradleExtensionMixInBuilder<T> forExtension(Class<T> extensionType);
}
