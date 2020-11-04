package dev.gradleplugins.runnerkit

class GradleExecutorGradleTestKitIntegrationTest extends AbstractGradleExecutorIntegrationTest {
    @Override
    GradleRunner newRunner() {
        return GradleRunner.create(GradleExecutor.gradleTestKit())
    }
}
