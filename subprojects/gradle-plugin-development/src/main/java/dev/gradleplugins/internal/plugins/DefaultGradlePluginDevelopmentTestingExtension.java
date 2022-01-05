package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestingExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

abstract /*final*/ class DefaultGradlePluginDevelopmentTestingExtension implements GradlePluginDevelopmentTestingExtension, HasPublicType {
    @Inject
    public DefaultGradlePluginDevelopmentTestingExtension() {

    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentTestingExtension.class);
    }
}
