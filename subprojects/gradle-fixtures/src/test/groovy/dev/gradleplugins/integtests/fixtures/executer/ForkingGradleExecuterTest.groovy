package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.runnerkit.GradleExecutor
import dev.gradleplugins.runnerkit.GradleRunner
import dev.gradleplugins.runnerkit.GradleWrapperFixture

//@Subject(ForkingGradleExecuter)
class ForkingGradleExecuterTest extends AbstractGradleExecuterTest {
    @Override
    protected GradleRunner getExecuterUnderTest() {
        GradleWrapperFixture.writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory);
//        return new ForkingGradleExecuter(GradleDistributionFactory.current(), testDirectory, DefaultGradleExecuterBuildContext.INSTANCE)
    }
}
