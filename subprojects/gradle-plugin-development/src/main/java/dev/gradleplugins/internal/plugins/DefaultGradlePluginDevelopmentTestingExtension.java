package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginDevelopmentTestSuiteFactory;
import dev.gradleplugins.GradlePluginDevelopmentTestingExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import lombok.val;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

/*final*/ class DefaultGradlePluginDevelopmentTestingExtension implements GradlePluginDevelopmentTestingExtension, HasPublicType {
    private final GradlePluginDevelopmentTestSuiteFactory factory;
    private final SoftwareComponentContainer components;

    @Inject
    public DefaultGradlePluginDevelopmentTestingExtension(GradlePluginDevelopmentTestSuiteFactory factory, SoftwareComponentContainer components) {
        this.factory = factory;
        this.components = components;
    }

    @Override
    public GradlePluginDevelopmentTestSuite registerSuite(String name) {
        val result = factory.create(name);
        components.add((GradlePluginDevelopmentTestSuiteInternal) result);
        return result;
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentTestingExtension.class);
    }
}
