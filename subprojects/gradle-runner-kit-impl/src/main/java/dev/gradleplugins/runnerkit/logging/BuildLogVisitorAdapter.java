package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.ActionableTaskCount;
import dev.gradleplugins.runnerkit.BuildOutcome;
import dev.gradleplugins.runnerkit.TaskOutcome;
import dev.gradleplugins.runnerkit.TaskPath;

public abstract class BuildLogVisitorAdapter implements BuildLogVisitor {
    private final BuildLogVisitor delegate;

    public BuildLogVisitorAdapter(BuildLogVisitor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void visitTaskHeader(TaskPath taskPath) {
        delegate.visitTaskHeader(taskPath);
    }

    @Override
    public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
        delegate.visitTaskHeader(taskPath, taskOutcome);
    }

    @Override
    public void visitTaskOutput(TaskPath taskPath, String line) {
        delegate.visitTaskOutput(taskPath, line);
    }

    @Override
    public void visitBuildResult(BuildOutcome buildOutcome) {
        delegate.visitBuildResult(buildOutcome);
    }

    @Override
    public void visitBuildFailure() {
        delegate.visitBuildFailure();
    }

    @Override
    public void visitActionableTasks(ActionableTaskCount actionableTaskCount) {
        delegate.visitActionableTasks(actionableTaskCount);
    }

    @Override
    public void visitContentLine(String line) {
        delegate.visitContentLine(line);
    }
}