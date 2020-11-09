package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.runnerkit.GradleExecutor
import dev.gradleplugins.runnerkit.GradleRunner

//@Subject(GradleRunnerExecuter)
class GradleRunnerExecuterTest extends AbstractGradleExecuterTest {
    @Override
    protected GradleRunner getExecuterUnderTest() {
        return GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory)
//        return new GradleRunnerExecuter(GradleDistributionFactory.current(), testDirectory, TestKitGradleExecuterBuildContext.INSTANCE)
    }

//    def "creates out-of-process executer when requiring distribution"() {
//        when:
//        def executer = executerUnderTest.requireGradleDistribution()
//
//        then:
//        executer instanceof ForkingGradleExecuter
//        executer.testDirectory == testDirectory
//        executer.distribution == executerUnderTest.distribution
//    }
}
