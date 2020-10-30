package dev.gradleplugins.fixtures.gradle.runner;

import dev.nokee.core.exec.CommandLineToolExecutionResult;

final class GradleExecutionResultNokeeExecImpl implements GradleExecutionResult {
    private final CommandLineToolExecutionResult delegate;

    public GradleExecutionResultNokeeExecImpl(CommandLineToolExecutionResult delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getOutput() {
        return delegate.getOutput().getAsString();
    }

    @Override
    public boolean isSuccessful() {
        return delegate.getExitValue() == 0;
    }
}
