package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin

class JavaGradlePluginDevelopmentTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentTestingStrategyFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin()
    }
}
