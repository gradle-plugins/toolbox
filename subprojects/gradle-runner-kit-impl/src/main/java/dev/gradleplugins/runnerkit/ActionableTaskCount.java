package dev.gradleplugins.runnerkit;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

import static dev.gradleplugins.runnerkit.TaskOutcomeUtils.isSkipped;

@EqualsAndHashCode
public final class ActionableTaskCount {
    @Getter private final long executedTaskCount;
    @Getter private final long upToDateTaskCount;

    private ActionableTaskCount(long executedTaskCount, long upToDateTaskCount) {
        this.executedTaskCount = executedTaskCount;
        this.upToDateTaskCount = upToDateTaskCount;
    }

    public long getActionableTaskCount() {
        return executedTaskCount + upToDateTaskCount;
    }

    public static ActionableTaskCount of(long executedTaskCount, long upToDateTaskCount) {
        return new ActionableTaskCount(executedTaskCount, upToDateTaskCount);
    }

    public static ActionableTaskCount from(Map<TaskPath, BuildTask> discoveredTasks) {
        return new ActionableTaskCount(
                discoveredTasks.values().stream().filter(ActionableTaskCount::isExecuted).count(),
                discoveredTasks.values().stream().filter(ActionableTaskCount::isUpToDate).count());
    }

    private static boolean isExecuted(BuildTask task) {
        return !isSkipped(task.getOutcome());
    }

    private static boolean isUpToDate(BuildTask task) {
        return isSkipped(task.getOutcome());
    }

    public void toString(StringBuilder result) {
        result.append(getActionableTaskCount()).append(" actionable ");
        if (getActionableTaskCount() > 1) {
            result.append("tasks");
        } else {
            result.append("task");
        }
        result.append(": ").append(getExecutedTaskCount()).append(" executed");
        if (getUpToDateTaskCount() > 0) {
            result.append(", ").append(getUpToDateTaskCount()).append(" up-to-date");
        }
    }
}