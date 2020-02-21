package dev.gradleplugins.integtests.fixtures.executer

class GradleRunnerExecutionResultTest extends AbstractExecutionResultTest {
    @Override
    protected GradleExecuter getExecuterUnderTest() {
        return new GradleRunnerExecuter(temporaryFolder)
    }
}
