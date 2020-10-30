package dev.gradleplugins.fixtures.gradle.runner

import static dev.gradleplugins.fixtures.gradle.runner.GradleWrapperFixture.writeGradleWrapperTo

class GradleExecutorGradleWrapperIntegrationTest extends AbstractGradleExecutorIntegrationTest {
    @Override
    GradleRunner newRunner() {
        System.getenv().each {k, v ->
            println("$k = $v")
        }
        writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).withDefaultLocale(Locale.getDefault())
    }
}
