package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin

class GroovyGradlePluginDevelopmentTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentTestingStrategyFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin()
    }
}
