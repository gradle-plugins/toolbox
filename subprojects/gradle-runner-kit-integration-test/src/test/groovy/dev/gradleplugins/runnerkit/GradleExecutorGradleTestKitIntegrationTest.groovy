package dev.gradleplugins.runnerkit

import org.gradle.testkit.runner.GradleRunner

class GradleExecutorGradleTestKitIntegrationTest extends AbstractGradleExecutorIntegrationTest {
    @Override
    GradleRunner newRunner() {
        return GradleRunner.create(GradleExecutor.gradleTestKit())
    }
}
