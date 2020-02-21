package dev.gradleplugins.integtests.fixtures.executer

class GradleRunnerExecutionFailureTest extends AbstractExecutionFailureTest {
    @Override
    protected GradleExecuter getExecuterUnderTest() {
        return new GradleRunnerExecuter(temporaryFolder)
    }
}
