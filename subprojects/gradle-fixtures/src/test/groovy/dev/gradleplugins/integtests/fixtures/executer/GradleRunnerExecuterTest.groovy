package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.internal.GradleRunnerExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutOfProcessGradleExecuter
import dev.gradleplugins.test.fixtures.gradle.executer.internal.TestKitGradleExecuterBuildContext
import spock.lang.Subject

@Subject(GradleRunnerExecuter)
class GradleRunnerExecuterTest extends AbstractGradleExecuterTest {
    @Override
    protected GradleExecuter getExecuterUnderTest() {
        return new GradleRunnerExecuter(GradleDistributionFactory.current(), testDirectory, TestKitGradleExecuterBuildContext.INSTANCE)
    }

    def "creates out-of-process executer when requiring distribution"() {
        when:
        def executer = executerUnderTest.requireGradleDistribution()

        then:
        executer instanceof OutOfProcessGradleExecuter
        executer.testDirectory == testDirectory
        executer.distribution == executerUnderTest.distribution
    }
}
