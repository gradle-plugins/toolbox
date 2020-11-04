package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.ActionableTaskCount;
import dev.gradleplugins.runnerkit.BuildOutcome;
import dev.gradleplugins.runnerkit.TaskOutcome;
import dev.gradleplugins.runnerkit.TaskPath;

public interface BuildLogVisitor {
    void visitTaskHeader(TaskPath taskPath);
    void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome);
    void visitTaskOutput(TaskPath taskPath, String line);
    void visitBuildResult(BuildOutcome buildOutcome);
    void visitBuildFailure();
    void visitActionableTasks(ActionableTaskCount actionableTaskCount);
    void visitContentLine(String line);
}
