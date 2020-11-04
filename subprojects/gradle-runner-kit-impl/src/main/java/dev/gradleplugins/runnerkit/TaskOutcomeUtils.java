package dev.gradleplugins.runnerkit;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TaskOutcomeUtils {
    private static final Map<String, TaskOutcome> TASK_OUTCOME_MAPPING = new LinkedHashMap<>();

    static {
        for (TaskOutcome value : TaskOutcome.values()) {
            TASK_OUTCOME_MAPPING.put(toString(value), value);
        }
    }

    private TaskOutcomeUtils() {}

    public static String toString(TaskOutcome outcome) {
        return outcome.name().replace('_', '-');
    }

    public static TaskOutcome from(@Nullable String value) {
        if (value == null) {
            return TaskOutcome.SUCCESS;
        }

        return TASK_OUTCOME_MAPPING.computeIfAbsent(value, key -> {
            throw unknownTaskOutcomeException(key);
        });
    }

    private static IllegalArgumentException unknownTaskOutcomeException(String value) {
        return new IllegalArgumentException(String.format("Unknown task outcome '%s'. Known values: (%s).", value, String.join(", ", TASK_OUTCOME_MAPPING.keySet())));
    }

    public static boolean isSkipped(TaskOutcome outcome) {
        return !(outcome.equals(TaskOutcome.SUCCESS) || outcome.equals(TaskOutcome.FAILED));
    }
}
