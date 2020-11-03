package dev.gradleplugins.runnerkit

import org.gradle.testkit.runner.GradleRunner

class GradleRunnerGradleWrapperIntegrationTest extends AbstractGradleRunnerIntegrationTest implements GradleWrapperFixture {
    @Override
    protected GradleRunner runner(String... arguments) {
        writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(new GradleExecutorGradleWrapperImpl()).inDirectory(testDirectory).withArguments(arguments)
    }
}
