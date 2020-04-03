package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin

class GroovyGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentExtensionFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin()
    }

    @Override
    protected Class<?> getExtraExtensionClass() {
        return GroovyGradlePluginDevelopmentExtension
    }
}
