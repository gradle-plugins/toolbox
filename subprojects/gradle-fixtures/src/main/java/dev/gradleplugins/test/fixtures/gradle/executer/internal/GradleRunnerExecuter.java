/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.logging.GroupedOutputFixture;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.hamcrest.Matcher;
import org.junit.Assert;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;

// TODO: This is implementation details and should be moved to internal package
//    We should instead offer a factory to construct the right executer
//    The contextual executer would also be beneficial here.
public class GradleRunnerExecuter extends AbstractGradleExecuter {
    public GradleRunnerExecuter(GradleDistribution distribution, TestFile testDirectory) {
        super(distribution, testDirectory);
    }

    private GradleRunnerExecuter(TestFile testDirectory, GradleExecuterConfiguration configuration) {
        super(testDirectory, configuration);
    }

    @Override
    protected GradleExecuter newInstance(TestFile testDirectory, GradleExecuterConfiguration configuration) {
        return new GradleRunnerExecuter(testDirectory, configuration);
    }

    @Override
    public GradleExecuter withDebuggerAttached() {
        return newInstance(configuration.withDebuggerAttached(true));
    }

    @Override
    public GradleExecuter withPluginClasspath() {
        return newInstance(configuration.withPluginClasspath(true));
    }

    @Override
    public GradleExecuter requireGradleDistribution() {
        return new OutOfProcessGradleExecuter(getTestDirectory(), configuration);
    }

    @Override
    public ExecutionResult doRun() {
        return new GradleRunnerExecutionResult(configureExecuter().build());
    }

    @Override
    public ExecutionFailure doRunWithFailure() {
        return new GradleRunnerExecutionFailure(configureExecuter().buildAndFail());
    }

    private GradleRunner configureExecuter() {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();

        if (configuration.isPluginClasspath()) {
            runner.withPluginClasspath();
        }
        runner.withProjectDir(getWorkingDirectory());

        if (configuration.isDebuggerAttached()) {
            System.out.println("WARNING: Gradle TestKit has some class loader issue that may result in runtime failures - such as NoClassDefFoundError for groovy/util/AntBuilder (see https://github.com/gradle/gradle/issues/1687).");
            runner.withDebug(true);
        }

        if (configuration.getDistribution() != null && !(configuration.getDistribution() instanceof CurrentGradleDistribution)) {
            runner.withGradleVersion(configuration.getDistribution().getVersion().getVersion());
        } else if (configuration.getGradleVersion() != null) {
            runner.withGradleVersion(configuration.getGradleVersion());
        }

        if (!configuration.getEnvironment().isEmpty()) {
            Map<String, String> environment = new HashMap<>(System.getenv());
            environment.putAll(configuration.getEnvironment().entrySet().stream().map(it -> new AbstractMap.SimpleImmutableEntry<>(it.getKey(), it.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            runner.withEnvironment(environment);
        }

        runner.withArguments(getAllArguments());

        return runner;
    }

    // TODO: This is not how we want to solve this use case!
    @Deprecated
    public GradleExecuter usingGradleVersion(String gradleVersion) {
        System.out.println("WARNING: Stop using this method, we are modling the GradleDistribution better to have a cross-executor solution");
        return newInstance(configuration.withGradleVersion(gradleVersion));
    }

    private static class GradleRunnerExecutionResult implements ExecutionResult {
        private final BuildResult result;
        private final OutputScrapingExecutionResult delegate;

        GradleRunnerExecutionResult(BuildResult result) {
            this.result = result;
            delegate = OutputScrapingExecutionResult.from(result.getOutput(), "");
        }

        private static List<String> flattenTaskPaths(Object[] taskPaths) {
            List<String> result = new ArrayList<>();
            flattenTaskPaths(Arrays.asList(taskPaths), result);
            return result;
        }

        private static void flattenTaskPaths(Collection<? super Object> taskPaths, List<String> flattenTaskPaths) {
            taskPaths.stream().forEach(it -> {
                if (it instanceof Collection) {
                    flattenTaskPaths((Collection<Object>)it, flattenTaskPaths);
                } else {
                    flattenTaskPaths.add(it.toString());
                }
            });
        }

        @Override
        public GroupedOutputFixture getGroupedOutput() {
            return delegate.getGroupedOutput();
        }

        @Override
        public String getPlainTextOutput() {
            return delegate.getPlainTextOutput();
        }

        @Override
        public String getOutput() {
            return delegate.getOutput();
        }

        @Override
        public ExecutionResult assertTaskNotExecuted(String taskPath) {
            Set<String> actualTasks = findExecutedTasksInOrderStarted();
            if (actualTasks.contains(taskPath)) {
                failOnMissingElement("Build output does contains unexpected task.", taskPath, actualTasks);
            }
            return this;
        }

        private void failOnMissingElement(String message, String expected, Set<String> actual) {
            failureOnUnexpectedOutput(String.format("%s%nExpected: %s%nActual: %s", message, expected, actual));
        }

        @Override
        public ExecutionResult assertTasksExecuted(Object... taskPaths) {
            Set<String> expectedTasks = new TreeSet<String>(flattenTaskPaths(taskPaths));
            Set<String> actualTasks = findExecutedTasksInOrderStarted();
            if (!expectedTasks.equals(actualTasks)) {
                failOnDifferentSets("Build output does not contain the expected tasks.", expectedTasks, actualTasks);
            }
            return this;
        }

        @Override
        public ExecutionResult assertTasksExecutedAndNotSkipped(Object... taskPaths) {
            assertTasksExecuted(taskPaths);
            return assertTasksNotSkipped(taskPaths);
        }

        @Override
        public ExecutionResult assertTasksNotSkipped(Object... taskPaths) {
            Set<String> expectedTasks = new TreeSet<String>(flattenTaskPaths(taskPaths));
            Set<String> tasks = new TreeSet<String>(getNotSkippedTasks());
            if (!expectedTasks.equals(tasks)) {
                failOnDifferentSets("Build output does not contain the expected non skipped tasks.", expectedTasks, tasks);
            }
            return this;
        }

        @Override
        public ExecutionResult assertTasksSkipped(Object... taskPaths) {
            Set<String> expectedTasks = new TreeSet<String>(flattenTaskPaths(taskPaths));
            Set<String> skippedTasks = getSkippedTasks();
            if (!expectedTasks.equals(skippedTasks)) {
                failOnDifferentSets("Build output does not contain the expected skipped tasks.", expectedTasks, skippedTasks);
            }
            return this;
        }

        private void failOnDifferentSets(String message, Set<String> expected, Set<String> actual) {
            failureOnUnexpectedOutput(String.format("%s%nExpected: %s%nActual: %s", message, expected, actual));
        }

        private void failureOnUnexpectedOutput(String message) {
            throw new AssertionError(unexpectedOutputMessage(message));
        }

        private String unexpectedOutputMessage(String message) {
            return String.format("%s%nOutput:%n=======%n%s%nError:%n======%n%s", message, getOutput(), "Using TestKit Runner which mixin both error output");
        }

        @Override
        public ExecutionResult assertOutputContains(String expectedOutput) {
            delegate.assertOutputContains(expectedOutput);
            return this;
        }

        @Override
        public ExecutionResult assertNotOutput(String expectedOutput) {
            delegate.assertNotOutput(expectedOutput);
            return this;
        }

        @Override
        public ExecutionResult assertHasPostBuildOutput(String expectedOutput) {
            delegate.assertHasPostBuildOutput(expectedOutput);
            return this;
        }

        private static final List<TaskOutcome> SKIPPED_TASK_OUTCOMES = Arrays.asList(TaskOutcome.FROM_CACHE, TaskOutcome.NO_SOURCE, TaskOutcome.SKIPPED, TaskOutcome.UP_TO_DATE);

        private Set<String> findExecutedTasksInOrderStarted() {
            return result.getTasks().stream().map(BuildTask::getPath).collect(Collectors.toCollection(TreeSet::new));
        }

        private Set<String> getSkippedTasks() {
            return result.getTasks().stream().filter(it -> SKIPPED_TASK_OUTCOMES.contains(it.getOutcome())).map(BuildTask::getPath).collect(Collectors.toCollection(TreeSet::new));
        }

        private List<String> getExecutedTasks() {
            return new ArrayList<>(findExecutedTasksInOrderStarted());
        }

        private Collection<String> getNotSkippedTasks() {
            Set<String> all = new TreeSet<String>(getExecutedTasks());
            Set<String> skipped = getSkippedTasks();
            all.removeAll(skipped);
            return all;
        }

        @Override
        public ExecutionResult assertTaskNotSkipped(String taskPath) {
            Assert.assertThat(getExecutedTasks(), hasItem(taskPath));
            return this;
        }

        @Override
        public ExecutionResult assertTaskSkipped(String taskPath) {
            Assert.assertThat(getSkippedTasks(), hasItem(taskPath));
            return this;
        }

        @Override
        public ExecutionResult assertThatOutput(Matcher<? super String> matcher) {
            return delegate.assertThatOutput(matcher);
        }
    }

    private static class GradleRunnerExecutionFailure extends GradleRunnerExecutionResult implements ExecutionFailure {
        private final OutputScrapingExecutionFailure delegate;

        GradleRunnerExecutionFailure(BuildResult result) {
            super(result);
            delegate = OutputScrapingExecutionFailure.from(result.getOutput(), result.getOutput());
        }

        @Override
        public ExecutionFailure assertHasCause(String description) {
            return delegate.assertHasCause(description);
        }

        @Override
        public ExecutionFailure assertThatCause(Matcher<? super String> matcher) {
            return delegate.assertThatCause(matcher);
        }

        @Override
        public ExecutionFailure assertHasDescription(String context) {
            return delegate.assertHasDescription(context);
        }
    }
}
