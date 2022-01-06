package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

abstract /*final*/ class DefaultGradlePluginDevelopmentCompatibilityExtension implements GradlePluginDevelopmentCompatibilityExtension, HasPublicType {
    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentCompatibilityExtension.class);
    }
}
