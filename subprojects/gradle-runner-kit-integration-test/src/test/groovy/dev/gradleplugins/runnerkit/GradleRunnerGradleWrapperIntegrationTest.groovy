package dev.gradleplugins.runnerkit

class GradleRunnerGradleWrapperIntegrationTest extends AbstractGradleRunnerIntegrationTest implements GradleWrapperFixture {
    @Override
    protected GradleRunner runner(String... arguments) {
        writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory).withArguments(arguments)
    }
}
