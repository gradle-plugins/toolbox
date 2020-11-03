package dev.gradleplugins.runnerkit

import org.gradle.testkit.runner.GradleRunner

class GradleExecutorGradleWrapperIntegrationTest extends AbstractGradleExecutorIntegrationTest {
    @Override
    GradleRunner newRunner() {
        System.getenv().each {k, v ->
            println("$k = $v")
        }
        dev.gradleplugins.runnerkit.GradleWrapperFixture.writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).withDefaultLocale(Locale.getDefault())
    }
}
