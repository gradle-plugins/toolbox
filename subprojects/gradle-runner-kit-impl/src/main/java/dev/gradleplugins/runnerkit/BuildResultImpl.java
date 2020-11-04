package dev.gradleplugins.runnerkit;

import dev.gradleplugins.runnerkit.logging.*;
import dev.nokee.core.exec.CommandLineToolLogContent;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

import static dev.gradleplugins.runnerkit.TaskOutcomeUtils.isSkipped;
import static dev.gradleplugins.runnerkit.logging.GradleLogContentUtils.*;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@EqualsAndHashCode
public final class BuildResultImpl implements BuildResult {
    private final Map<TaskPath, BuildTask> executedTaskInOrder;
    private final ActionableTaskCount actionableTaskCount;
    private final BuildOutcome buildOutcome;
    private final BuildFailures failures; // for now we exclude the failures
    @EqualsAndHashCode.Exclude private final CommandLineToolLogContent output;

    public BuildResultImpl(Map<TaskPath, BuildTask> executedTaskInOrder, CommandLineToolLogContent output, ActionableTaskCount actionableTaskCount, BuildOutcome buildOutcome, BuildFailures failures) {
        this.executedTaskInOrder = executedTaskInOrder;
        this.output = output;
        this.actionableTaskCount = actionableTaskCount;
        this.buildOutcome = buildOutcome;
        this.failures = failures;
    }

    public BuildOutcome getOutcome() {
        return buildOutcome;
    }

    @Override
    public String toString() {
        val result = new StringBuilder();
        for (val task : executedTaskInOrder.values()) {
            ((BuildTaskImpl) task).toString(result);
            result.append("\n");
        }

        result.append("\n"); // division

        if (buildOutcome.equals(BuildOutcome.FAILED)) {
            failures.toString(result);
            result.append("\n");
            result.append("\n"); // division
        }
        result.append("BUILD ").append(getOutcome().name()).append("\n");
        actionableTaskCount.toString(result);

        return result.toString();
    }

    @Override
    public String getOutput() {
        return output.getAsString();
    }

    @Override
    public List<String> getExecutedTaskPaths() {
        return unmodifiableList(executedTaskInOrder.values().stream().map(BuildResultImpl::toPath).collect(toList()));
    }

    @Override
    public List<String> getSkippedTaskPaths() {
        return unmodifiableList(executedTaskInOrder.values().stream().filter(it -> isSkipped(it.getOutcome())).map(BuildResultImpl::toPath).collect(toList()));
    }

    private static String toPath(BuildTask buildTask) {
        return buildTask.getPath();
    }

    @Override
    public List<BuildTask> getTasks() {
        return unmodifiableList(new ArrayList<>(executedTaskInOrder.values()));
    }

    @Override
    public List<BuildTask> tasks(TaskOutcome outcome) {
        return unmodifiableList(executedTaskInOrder.values().stream().filter(it -> it.getOutcome().equals(outcome)).collect(toList()));
    }

    @Nullable
    @Override
    public BuildTask task(String taskPath) {
        return executedTaskInOrder.get(TaskPath.of(taskPath));
    }

    @Override
    public BuildResult withNormalizedTaskOutput(Predicate<TaskPath> predicate, UnaryOperator<String> outputNormalizer) {
        Map<TaskPath, BuildTask> tasks = unmodifiableMap(executedTaskInOrder.entrySet().stream().map(entry -> {
            if (predicate.test(entry.getKey())) {
                return new LinkedHashMap.SimpleEntry<>(entry.getKey(), new BuildTaskImpl(entry.getKey(), entry.getValue().getOutcome(), outputNormalizer.apply(entry.getValue().getOutput())));
            }
            return entry;
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), LinkedHashMap::new)));
        return new BuildResultImpl(tasks, output, actionableTaskCount, buildOutcome, failures);
    }

    @Override
    public BuildResult withoutBuildSrc() {
        Map<TaskPath, BuildTask> tasks = unmodifiableMap(executedTaskInOrder.entrySet().stream().filter(entry -> {
            return !entry.getKey().getProjectPath().equals(":buildSrc");
        }).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), LinkedHashMap::new)));
        return new BuildResultImpl(tasks, output, actionableTaskCount, buildOutcome, failures);
    }

    @Override
    public BuildResult asRichOutputResult() {
        Map<TaskPath, BuildTask> tasks = unmodifiableMap(executedTaskInOrder.entrySet().stream().filter(it -> !it.getValue().getOutput().isEmpty()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), LinkedHashMap::new)));
        return new BuildResultImpl(tasks, output, actionableTaskCount, buildOutcome, failures);
    }

    @Override
    public List<Failure> getFailures() {
        return failures.get();
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
}
