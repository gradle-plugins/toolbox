package dev.gradleplugins.internal.runtime.dsl;

import org.gradle.api.plugins.ExtensionAware;

public interface GradleExtensionMixIn {
    void mixInto(ExtensionAware instance);
}
