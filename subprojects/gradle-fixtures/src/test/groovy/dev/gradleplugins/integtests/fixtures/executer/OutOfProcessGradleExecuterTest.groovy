package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutOfProcessGradleExecuter
import spock.lang.Subject

@Subject(OutOfProcessGradleExecuter)
class OutOfProcessGradleExecuterTest extends AbstractGradleExecuterTest {
    @Override
    protected GradleExecuter getExecuterUnderTest() {
        return new OutOfProcessGradleExecuter(GradleDistributionFactory.current(), testDirectory)
    }
}
