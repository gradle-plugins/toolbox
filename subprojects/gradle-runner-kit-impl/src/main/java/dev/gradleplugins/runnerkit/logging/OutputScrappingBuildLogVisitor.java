package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

public final class OutputScrappingBuildLogVisitor implements BuildLogVisitor {
    private final Map<TaskPath, BuildTaskBuilder> discoveredTasks = new LinkedHashMap<>();
    private ActionableTaskCount actionableTaskCount = null;
    private BuildOutcome buildOutcome = null;

    public Optional<ActionableTaskCount> getActionableTaskCount() {
        return Optional.ofNullable(actionableTaskCount);
    }

    public BuildOutcome getBuildOutcome() {
        return buildOutcome;
    }

    public Map<TaskPath, BuildTask> getDiscoveredTasks() {
        return unmodifiableMap(discoveredTasks.entrySet().stream().collect(toMap(Map.Entry::getKey, it -> it.getValue().build(), throwingMerger(), LinkedHashMap::new)));
    }

    public void visitTaskHeader(TaskPath taskPath) {
        discoveredTasks.computeIfAbsent(taskPath, this::newTaskBuilder);
    }

    private BuildTaskBuilder newTaskBuilder(TaskPath taskPath) {
        return BuildTaskBuilder.newBuilder().withPath(taskPath);
    }

    public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
        discoveredTasks.compute(taskPath, updateOutcome(taskOutcome));
    }

    private static BiFunction<TaskPath, BuildTaskBuilder, BuildTaskBuilder> updateOutcome(TaskOutcome taskOutcome) {
        return (taskPath, buildTask) -> {
            if (buildTask == null) {
                return BuildTaskBuilder.newBuilder().withPath(taskPath).withOutcome(taskOutcome);
            }
            return buildTask.withOutcome(taskOutcome);
        };
    }

    @Override
    public void visitTaskOutput(TaskPath taskPath, String line) {
        discoveredTasks.compute(taskPath, appendContent(line));
    }

    @Override
    public void visitBuildResult(BuildOutcome buildOutcome) {
        this.buildOutcome = buildOutcome;
    }

    @Override
    public void visitBuildFailure() { /* currently handled by BuildFailures */ }

    @Override
    public void visitActionableTasks(ActionableTaskCount actionableTaskCount) {
        this.actionableTaskCount = actionableTaskCount;
    }

    @Override
    public void visitContentLine(String line) {}

    private static BiFunction<TaskPath, BuildTaskBuilder, BuildTaskBuilder> appendContent(String line) {
        return (taskPath, buildTask) -> {
            assert buildTask != null;
            return buildTask.appendToOutput(line);
        };
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
}
