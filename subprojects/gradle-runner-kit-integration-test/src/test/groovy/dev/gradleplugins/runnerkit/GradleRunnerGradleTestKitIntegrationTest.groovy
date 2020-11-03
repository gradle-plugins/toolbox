package dev.gradleplugins.runnerkit

import org.gradle.testkit.runner.GradleRunner

class GradleRunnerGradleTestKitIntegrationTest extends AbstractGradleRunnerIntegrationTest {
    @Override
    protected GradleRunner runner(String... arguments) {
        return GradleRunner.create(new GradleExecutorGradleTestKitImpl()).inDirectory(testDirectory).withArguments(arguments)
    }
}
