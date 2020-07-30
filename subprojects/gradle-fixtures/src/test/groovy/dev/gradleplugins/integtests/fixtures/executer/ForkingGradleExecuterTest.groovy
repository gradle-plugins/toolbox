package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.internal.DefaultGradleExecuterBuildContext
import dev.gradleplugins.test.fixtures.gradle.executer.internal.ForkingGradleExecuter
import spock.lang.Subject

@Subject(ForkingGradleExecuter)
class ForkingGradleExecuterTest extends AbstractGradleExecuterTest {
    @Override
    protected GradleExecuter getExecuterUnderTest() {
        return new ForkingGradleExecuter(GradleDistributionFactory.current(), testDirectory, DefaultGradleExecuterBuildContext.INSTANCE)
    }
}
