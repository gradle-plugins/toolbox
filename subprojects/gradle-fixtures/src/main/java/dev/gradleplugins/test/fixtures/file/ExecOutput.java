package dev.gradleplugins.test.fixtures.file;

public class ExecOutput {
    public ExecOutput(int exitCode, String rawOutput, String error) {
        this.exitCode = exitCode;
        this.rawOutput = rawOutput;
        this.out = rawOutput.replaceAll("\r\n|\r", "\n");
        this.error = error;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    public void setRawOutput(String rawOutput) {
        this.rawOutput = rawOutput;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    private int exitCode;
    private String rawOutput;
    private String out;
    private String error;
}
