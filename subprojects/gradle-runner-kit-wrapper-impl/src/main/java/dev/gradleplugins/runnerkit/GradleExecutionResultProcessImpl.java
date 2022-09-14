package dev.gradleplugins.runnerkit;

final class GradleExecutionResultProcessImpl implements GradleExecutionResult {
    private final int exitValue;
    private final String output;

    public GradleExecutionResultProcessImpl(int exitValue, String output) {
        this.exitValue = exitValue;
        this.output = output;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public boolean isSuccessful() {
        return exitValue == 0;
    }
}
