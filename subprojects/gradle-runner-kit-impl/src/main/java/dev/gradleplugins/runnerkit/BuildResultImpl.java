package dev.gradleplugins.runnerkit;

import dev.nokee.core.exec.CommandLineToolLogContent;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@EqualsAndHashCode
final class BuildResultImpl implements BuildResult {
    private final Map<TaskPath, BuildTask> executedTaskInOrder;
    private final ActionableTaskCount actionableTaskCount;
    @EqualsAndHashCode.Exclude private final CommandLineToolLogContent output;

    BuildResultImpl(Map<TaskPath, BuildTask> executedTaskInOrder, CommandLineToolLogContent output, ActionableTaskCount actionableTaskCount) {
        this.executedTaskInOrder = executedTaskInOrder;
        this.output = output;
        this.actionableTaskCount = actionableTaskCount;
    }

    public static BuildResult from(String output) {
        return from(CommandLineToolLogContent.of(output));
    }

    public static BuildResult from(CommandLineToolLogContent output) {
        val normalizedOutput = normalize(output);

        val visitor = new TaskCollector();
        normalizedOutput.visitEachLine(new TaskOutputVisitorAdapter(new NewLineAdjuster(visitor)));

        val discoveredTasks = visitor.getDiscoveredTasks();
        val actionableTaskCount = visitor.getActionableTaskCount().orElseGet(() -> ActionableTaskCount.from(discoveredTasks));

        return new BuildResultImpl(discoveredTasks, normalizedOutput, actionableTaskCount);
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
        return unmodifiableList(executedTaskInOrder.values().stream().filter(it -> it.getOutcome().isSkipped()).map(BuildResultImpl::toPath).collect(toList()));
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
        return new BuildResultImpl(tasks, output, actionableTaskCount);
    }

    @Override
    public BuildResult asRichOutputResult() {
        Map<TaskPath, BuildTask> tasks = unmodifiableMap(executedTaskInOrder.entrySet().stream().filter(it -> !it.getValue().getOutput().isEmpty()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), LinkedHashMap::new)));
        return new BuildResultImpl(tasks, output, actionableTaskCount);
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }

    private interface TaskOutputVisitor {
        void visitTaskHeader(TaskPath taskPath);
        void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome);
        void visitTaskOutput(TaskPath taskPath, String line);
        void visitBuildResult();
        void visitActionableTasks(ActionableTaskCount actionableTaskCount);
        void visitContentLine(String line);
    }

    private static final class TaskCollector implements TaskOutputVisitor {
        private final Map<TaskPath, BuildTaskImpl.Builder>  discoveredTasks = new LinkedHashMap<>();
        private ActionableTaskCount actionableTaskCount = null;

        Optional<ActionableTaskCount> getActionableTaskCount() {
            return Optional.ofNullable(actionableTaskCount);
        }

        Map<TaskPath, BuildTask> getDiscoveredTasks() {
            return unmodifiableMap(discoveredTasks.entrySet().stream().collect(toMap(Map.Entry::getKey, it -> it.getValue().build(), throwingMerger(), LinkedHashMap::new)));
        }

        public void visitTaskHeader(TaskPath taskPath) {
            discoveredTasks.computeIfAbsent(taskPath, this::newTaskBuilder);
        }

        private BuildTaskImpl.Builder newTaskBuilder(TaskPath taskPath) {
            return BuildTaskImpl.builder().withPath(taskPath);
        }

        public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
            discoveredTasks.compute(taskPath, updateOutcome(taskOutcome));
        }

        private BiFunction<TaskPath, BuildTaskImpl.Builder, BuildTaskImpl.Builder> updateOutcome(TaskOutcome taskOutcome) {
            return (taskPath, buildTask) -> {
                if (buildTask == null) {
                    return BuildTaskImpl.builder().withPath(taskPath).withOutcome(taskOutcome);
                }
                return buildTask.withOutcome(taskOutcome);
            };
        }

        @Override
        public void visitTaskOutput(TaskPath taskPath, String line) {
            discoveredTasks.compute(taskPath, appendContent(line));
        }

        public void visitBuildResult() {}

        @Override
        public void visitActionableTasks(ActionableTaskCount actionableTaskCount) {
            this.actionableTaskCount = actionableTaskCount;
        }

        public void visitContentLine(String line) {}

        private BiFunction<TaskPath, BuildTaskImpl.Builder, BuildTaskImpl.Builder> appendContent(String line) {
            return (taskPath, buildTask) -> {
                assert buildTask != null;
                return buildTask.appendToOutput(line);
            };
        }
    }

    private static final class NewLineAdjuster implements TaskOutputVisitor {
        private final TaskOutputVisitor delegate;
        private TaskPath currentTask = null;
        private String previousLine = null;
        private boolean hasNoOutputSinceHeader = true;

        NewLineAdjuster(TaskOutputVisitor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void visitTaskHeader(TaskPath taskPath) {
            if (previousLine != null) {
                if (previousLine.isEmpty()) {
                    if (!hasNoOutputSinceHeader) {
                        delegate.visitTaskOutput(currentTask, "\n");
                    }
                } else {
                    delegate.visitTaskOutput(currentTask, previousLine);
                }
                previousLine = null;
            }
            currentTask = taskPath;
            hasNoOutputSinceHeader = true;
            delegate.visitTaskHeader(taskPath);
        }

        @Override
        public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
            if (previousLine != null) {
                if (previousLine.isEmpty()) {
                    if (!hasNoOutputSinceHeader) {
                        delegate.visitTaskOutput(currentTask, "\n");
                    }
                } else {
                    delegate.visitTaskOutput(currentTask, previousLine);
                }
                previousLine = null;
            }
            currentTask = taskPath;
            hasNoOutputSinceHeader = true;
            delegate.visitTaskHeader(taskPath, taskOutcome);
        }

        @Override
        public void visitTaskOutput(TaskPath taskPath, String line) {
            hasNoOutputSinceHeader = false;
            delegate.visitTaskOutput(taskPath, line);
        }

        @Override
        public void visitBuildResult() {
            if (previousLine != null) {
                if (previousLine.isEmpty()) {
                    if (!hasNoOutputSinceHeader) {
                        delegate.visitTaskOutput(currentTask, "\n");
                    }
                } else {
                    delegate.visitTaskOutput(currentTask, previousLine);
                }
                previousLine = null;
            }
            currentTask = null;
            hasNoOutputSinceHeader = false;
            delegate.visitBuildResult();
        }

        @Override
        public void visitActionableTasks(ActionableTaskCount actionableTaskCount) {
            delegate.visitActionableTasks(actionableTaskCount);
        }

        @Override
        public void visitContentLine(String line) {
            if (previousLine != null) {
                delegate.visitTaskOutput(currentTask, previousLine + "\n");
                previousLine = null;
            }
            if (currentTask != null) {
                previousLine = line;
            } else {
                delegate.visitContentLine(line);
            }
        }
    }

    private static final class CurrentTaskAwareVisitor implements TaskOutputVisitor {
        private final TaskOutputVisitor delegate;
        private TaskPath currentTask = null;

        CurrentTaskAwareVisitor(TaskOutputVisitor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void visitTaskHeader(TaskPath taskPath) {
            currentTask = taskPath;
            delegate.visitTaskHeader(taskPath);
        }

        @Override
        public void visitTaskHeader(TaskPath taskPath, TaskOutcome taskOutcome) {
            currentTask = taskPath;
            delegate.visitTaskHeader(taskPath, taskOutcome);
        }

        @Override
        public void visitTaskOutput(TaskPath taskPath, String line) {
            delegate.visitTaskOutput(taskPath, line);
        }

        @Override
        public void visitBuildResult() {
            currentTask = null;
            delegate.visitBuildResult();
        }

        @Override
        public void visitActionableTasks(ActionableTaskCount actionableTaskCount) {
            delegate.visitActionableTasks(actionableTaskCount);
        }

        @Override
        public void visitContentLine(String line) {
            if (currentTask != null) {
                delegate.visitTaskOutput(currentTask, line);
            } else {
                delegate.visitContentLine(line);
            }
        }
    }

//    private static final class ContentLineIgnorer implements TaskOutputVisitor {
//        private final TaskOutputVisitor delegate;
//        private boolean previousLineWasEmpty = false;
//
//        ContentLineIgnorer(TaskOutputVisitor delegate) {
//            this.delegate = delegate;
//        }
//
//        @Override
//        public void visitTaskHeader(GradleTaskPath taskPath) {
//            previousLineWasEmpty = false;
//            delegate.visitTaskHeader(taskPath);
//        }
//
//        @Override
//        public void visitTaskHeader(GradleTaskPath taskPath, GradleTaskOutcome taskOutcome) {
//            previousLineWasEmpty = false;
//            delegate.visitTaskHeader(taskPath, taskOutcome);
//        }
//
//        @Override
//        public void visitTaskOutput(GradleTaskPath taskPath, String line) {
//            delegate.visitTaskOutput(taskPath, line);
//        }
//
//        @Override
//        public void visitBuildResult() {
//            previousLineWasEmpty = false;
//            delegate.visitBuildResult();
//        }
//
//        @Override
//        public void visitContentLine(String line) {
//            if (previousLineWasEmpty) {
//                delegate.visitContentLine("");
//            }
//            if (line.isEmpty()) {
//                previousLineWasEmpty = true;
//            } else {
//                previousLineWasEmpty = false;
//                delegate.visitContentLine(line);
//            }
//        }
//    }

    private static final class TaskOutputVisitorAdapter implements Consumer<CommandLineToolLogContent.LineDetails> {
        //for example: ':hey' or ':a SKIPPED' or ':foo:bar:baz UP-TO-DATE' but not ':a FOO'
        private static final Pattern TASK_PATTERN = Pattern.compile("(> Task )?(:\\S+)\\s*(SKIPPED|UP-TO-DATE|FROM-CACHE|NO-SOURCE|FAILED)?");
        private static final Pattern ACTIONABLE_TASKS_PATTERN = Pattern.compile("(\\d+) actionable tasks?: (\\d+) executed(, (\\d+) up-to-date)?");

        private final TaskOutputVisitor visitor;

        TaskOutputVisitorAdapter(TaskOutputVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void accept(CommandLineToolLogContent.LineDetails details) {
            val matches = TASK_PATTERN.matcher(details.getLine());
            if (matches.matches()) {
                val taskPath = matches.group(2);
                val taskOutcome = matches.group(3);
                if (taskOutcome == null) {
                    visitor.visitTaskHeader(TaskPath.of(taskPath));
                } else {
                    visitor.visitTaskHeader(TaskPath.of(taskPath), TaskOutcome.of(taskOutcome));
                }
            } else if (details.getLine().equals("BUILD SUCCESSFUL")) {
                visitor.visitBuildResult();
            } else if (details.getLine().equals("BUILD FAILED")) {
                visitor.visitBuildResult();
            } else if (ACTIONABLE_TASKS_PATTERN.matcher(details.getLine()).matches()) {
                val m = ACTIONABLE_TASKS_PATTERN.matcher(details.getLine());
                m.matches();
                val actionableTaskCount = Integer.parseInt(m.group(1));
                val executedTaskCount = Integer.parseInt(m.group(2));
                val upToDateTaskCount = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
                assert actionableTaskCount == (executedTaskCount + upToDateTaskCount);
                visitor.visitActionableTasks(new ActionableTaskCount(executedTaskCount, upToDateTaskCount));
            } else {
                visitor.visitContentLine(details.getLine());
            }
        }
    }

    private static CommandLineToolLogContent normalize(CommandLineToolLogContent content) {
        return content.withAnsiControlCharactersInterpreted().withNormalizedEndOfLine().visitEachLine(JavaIllegalAccessWarningsStripper.INSTANCE).visitEachLine(GradleDaemonMessageStripper.INSTANCE).visitEachLine(WarningSummaryMessageStripper.INSTANCE).visitEachLine(BuildResultNormalizer.INSTANCE);
    }

    private enum JavaIllegalAccessWarningsStripper implements Consumer<CommandLineToolLogContent.LineDetails> {
        INSTANCE;

        @Override
        public void accept(CommandLineToolLogContent.LineDetails details) {
            if (details.getLine().startsWith("WARNING: An illegal reflective access operation has occurred") || details.getLine().equals("WARNING: All illegal access operations will be denied in a future release")) {
                details.dropLine();
            }
        }
    }

    private enum GradleDaemonMessageStripper implements Consumer<CommandLineToolLogContent.LineDetails> {
        INSTANCE;

        // See org.gradle.launcher.daemon.client.DaemonStartupMessage#STARTING_DAEMON_MESSAGE
        static final String STARTING_DAEMON_MESSAGE = "Starting a Gradle Daemon";

        // See org.gradle.launcher.daemon.server.DaemonStateCoordinator#DAEMON_WILL_STOP_MESSAGE
        static final String DAEMON_WILL_STOP_MESSAGE = "Daemon will be stopped at the end of the build ";

        // See org.gradle.launcher.daemon.server.health.LowHeapSpaceDaemonExpirationStrategy#EXPIRE_DAEMON_MESSAGE
        static final String EXPIRE_DAEMON_MESSAGE = "Expiring Daemon because JVM heap space is exhausted";


        @Override
        public void accept(CommandLineToolLogContent.LineDetails details) {
            val line = details.getLine();
            if (line.contains(STARTING_DAEMON_MESSAGE)) {
                // Remove the "daemon starting" message
                details.dropLine();
            } else if (line.contains(DAEMON_WILL_STOP_MESSAGE)) {
                // Remove the "Daemon will be shut down" message
                details.dropLine();
            } else if (line.contains(EXPIRE_DAEMON_MESSAGE)) {
                // Remove the "Expiring Daemon" message
                details.dropLine();
            }
        }
    }

    private enum WarningSummaryMessageStripper implements Consumer<CommandLineToolLogContent.LineDetails> {
        INSTANCE;

        // See org.gradle.internal.featurelifecycle.LoggingDeprecatedFeatureHandler#WARNING_SUMMARY
        private static final String WARNING_SUMMARY = "Deprecated Gradle features were used in this build, making it incompatible with Gradle";

        @Override
        public void accept(CommandLineToolLogContent.LineDetails details) {
            if (details.getLine().contains(WARNING_SUMMARY)) {
                // Remove the deprecations message: "Deprecated Gradle features...", "Use '--warning-mode all'...", "See https://docs.gradle.org...", and additional newline
                details.drop(4);
            }
        }
    }

    private enum BuildResultNormalizer implements Consumer<CommandLineToolLogContent.LineDetails> {
        INSTANCE;

        private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED) in( \\d+m?[smh])+");

        @Override
        public void accept(CommandLineToolLogContent.LineDetails details) {
            if (BUILD_RESULT_PATTERN.matcher(details.getLine()).matches()) {
                details.replaceWith(BUILD_RESULT_PATTERN.matcher(details.getLine()).replaceFirst("BUILD $1"));
            }
        }
    }

    @EqualsAndHashCode
    private static final class ActionableTaskCount {
        final long executedTaskCount;
        final long upToDateTaskCount;

        ActionableTaskCount(long executedTaskCount, long upToDateTaskCount) {
            this.executedTaskCount = executedTaskCount;
            this.upToDateTaskCount = upToDateTaskCount;
        }

        public static ActionableTaskCount from(Map<TaskPath, BuildTask> discoveredTasks) {
            return new ActionableTaskCount(
                    discoveredTasks.values().stream().filter(ActionableTaskCount::isExecuted).count(),
                    discoveredTasks.values().stream().filter(ActionableTaskCount::isUpToDate).count());
        }

        private static boolean isExecuted(BuildTask task) {
            return !task.getOutcome().isSkipped();
        }

        private static boolean isUpToDate(BuildTask task) {
            return task.getOutcome().isSkipped();
        }
    }
}
