package dev.gradleplugins.runnerkit

class GradleRunnerGradleTestKitIntegrationTest extends AbstractGradleRunnerIntegrationTest {
    @Override
    protected GradleRunner runner(String... arguments) {
        return GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory).withArguments(arguments)
    }
}
