package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin

class JavaGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentExtensionFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin()
    }

    @Override
    protected Class<?> getExtraExtensionClass() {
        return JavaGradlePluginDevelopmentExtension
    }
}
