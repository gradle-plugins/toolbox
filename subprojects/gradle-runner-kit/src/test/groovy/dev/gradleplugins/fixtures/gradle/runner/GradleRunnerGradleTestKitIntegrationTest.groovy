package dev.gradleplugins.fixtures.gradle.runner

class GradleRunnerGradleTestKitIntegrationTest extends AbstractGradleRunnerIntegrationTest {
    @Override
    protected GradleRunner runner(String... arguments) {
        return GradleRunner.create(new GradleExecutorGradleTestKitImpl()).inDirectory(testDirectory).withArguments(arguments)
    }
}
