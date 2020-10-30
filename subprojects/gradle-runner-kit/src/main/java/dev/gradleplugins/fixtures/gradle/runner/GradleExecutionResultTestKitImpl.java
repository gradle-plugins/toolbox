package dev.gradleplugins.fixtures.gradle.runner;

final class GradleExecutionResultTestKitImpl implements GradleExecutionResult {
    private final org.gradle.testkit.runner.internal.GradleExecutionResult delegate;

    public GradleExecutionResultTestKitImpl(org.gradle.testkit.runner.internal.GradleExecutionResult delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getOutput() {
        return delegate.getOutput();
    }

    @Override
    public boolean isSuccessful() {
        return delegate.isSuccessful();
    }
}
