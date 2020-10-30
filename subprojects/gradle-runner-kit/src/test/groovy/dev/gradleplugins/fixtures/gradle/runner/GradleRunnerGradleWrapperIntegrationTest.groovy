package dev.gradleplugins.fixtures.gradle.runner

class GradleRunnerGradleWrapperIntegrationTest extends AbstractGradleRunnerIntegrationTest implements GradleWrapperFixture {
    @Override
    protected GradleRunner runner(String... arguments) {
        writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(new GradleExecutorGradleWrapperImpl()).inDirectory(testDirectory).withArguments(arguments)
    }
}
