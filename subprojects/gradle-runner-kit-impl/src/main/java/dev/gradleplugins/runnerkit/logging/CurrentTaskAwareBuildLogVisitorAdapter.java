package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.BuildOutcome;
import dev.gradleplugins.runnerkit.TaskOutcome;
import dev.gradleplugins.runnerkit.TaskPath;

public abstract class CurrentTaskAwareBuildLogVisitorAdapter extends BuildLogVisitorAdapter {
    private TaskPath currentTask = null;

    public CurrentTaskAwareBuildLogVisitorAdapter(BuildLogVisitor delegate) {
        super(delegate);
    }

    public TaskPath getCurrentTask() {
        return currentTask;
    }

    @Override
    public void visitTaskHeader(TaskPath taskPath) {
        currentTask = taskPath;
        super.visitTaskHeader(taskPath);
    }

    @Override
    public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
        currentTask = taskPath;
        super.visitTaskHeader(taskPath, taskOutcome);
    }

    @Override
    public void visitBuildResult(BuildOutcome buildOutcome) {
        currentTask = null;
        super.visitBuildResult(buildOutcome);
    }

    @Override
    public void visitBuildFailure() {
        currentTask = null;
        super.visitBuildFailure();
    }
}