package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.BuildOutcome;
import dev.gradleplugins.runnerkit.TaskOutcome;
import dev.gradleplugins.runnerkit.TaskPath;

public final class TaskOutputGroupingVisitor extends CurrentTaskAwareBuildLogVisitorAdapter {
    private String previousLine = null;
    private boolean hasOutputSinceHeader = false;

    public TaskOutputGroupingVisitor(BuildLogVisitor delegate) {
        super(delegate);
    }

    private void flushTaskOutput() {
        if (previousLine != null) {
            if (previousLine.isEmpty()) {
                if (hasOutputSinceHeader) {
                    super.visitTaskOutput(getCurrentTask(), "\n");
                }
            } else {
                super.visitTaskOutput(getCurrentTask(), previousLine);
            }
            previousLine = null;
        }
    }

    @Override
    public void visitTaskHeader(TaskPath taskPath) {
        flushTaskOutput();
        hasOutputSinceHeader = false;
        super.visitTaskHeader(taskPath);
    }

    @Override
    public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
        flushTaskOutput();
        hasOutputSinceHeader = false;
        super.visitTaskHeader(taskPath, taskOutcome);
    }

    @Override
    public void visitTaskOutput(TaskPath taskPath, String line) {
        hasOutputSinceHeader = true;
        super.visitTaskOutput(taskPath, line);
    }

    @Override
    public void visitBuildResult(BuildOutcome buildOutcome) {
        flushTaskOutput();
        hasOutputSinceHeader = false;
        super.visitBuildResult(buildOutcome);
    }

    @Override
    public void visitBuildFailure() {
        flushTaskOutput();
        hasOutputSinceHeader = false;
        super.visitBuildFailure();
    }

    @Override
    public void visitContentLine(String line) {
        if (previousLine != null) {
            super.visitTaskOutput(getCurrentTask(), previousLine + "\n");
            previousLine = null;
        }
        if (getCurrentTask() != null) {
            previousLine = line;
        } else {
            super.visitContentLine(line);
        }
    }
}