package dev.gradleplugins.runnerkit

class GradleExecutorGradleWrapperIntegrationTest extends AbstractGradleExecutorIntegrationTest {
    @Override
    GradleRunner newRunner() {
        GradleWrapperFixture.writeGradleWrapperTo(testDirectory)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).withDefaultLocale(Locale.getDefault())
    }
}
