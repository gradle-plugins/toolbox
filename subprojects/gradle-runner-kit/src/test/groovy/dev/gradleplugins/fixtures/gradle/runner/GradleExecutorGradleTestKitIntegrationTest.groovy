package dev.gradleplugins.fixtures.gradle.runner

class GradleExecutorGradleTestKitIntegrationTest extends AbstractGradleExecutorIntegrationTest {
    @Override
    GradleRunner newRunner() {
        return GradleRunner.create(GradleExecutor.gradleTestKit())
    }
}
