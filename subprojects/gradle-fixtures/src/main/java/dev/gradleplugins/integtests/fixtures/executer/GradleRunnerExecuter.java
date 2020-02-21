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

package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

// TODO: This is implementation details and should be moved to internal package
//    We should instead offer a factory to construct the right executer
//    The contextual executer would also be beneficial here.
public class GradleRunnerExecuter extends AbstractGradleExecuter {
    private boolean debuggerAttached = false;

    private String gradleVersion = null;
    private boolean usePluginClasspath = false;
    private Map<String, Object> environment = null;

    public GradleRunnerExecuter(TestDirectoryProvider testDirectoryProvider) {
        super(testDirectoryProvider);
    }

    @Override
    public GradleExecuter withDebuggerAttached() {
        debuggerAttached = true;
        return this;
    }

    @Override
    public GradleExecuter withPluginClasspath() {
        usePluginClasspath = true;
        return this;
    }

    @Override
    public GradleExecuter withEnvironmentVars(Map<String, ?> environment) {
        this.environment = new HashMap<>(environment);
        return this;
    }

    @Override
    public ExecutionResult doRun() {
        return new GradleRunnerExecutionResult(configureExecuter().build());
    }

    @Override
    public ExecutionFailure doRunWithFailure() {
        return new GradleRunnerExecutionFailure(configureExecuter().buildAndFail());
    }

    @Override
    protected void reset() {
        super.reset();
        debuggerAttached = false;
        gradleVersion = null;
        usePluginClasspath = false;
        environment = null;
    }

    private GradleRunner configureExecuter() {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();

        if (usePluginClasspath) {
            runner.withPluginClasspath();
        }
        runner.withProjectDir(getWorkingDirectory());

        if (debuggerAttached) {
            System.out.println("WARNING: Gradle TestKit has some class loader issue that may result in runtime failures - such as NoClassDefFoundError for groovy/util/AntBuilder (see https://github.com/gradle/gradle/issues/1687).");
            runner.withDebug(true);
        }

        if (gradleVersion != null) {
            runner.withGradleVersion(gradleVersion);
        }

        if (environment != null) {
            runner.withEnvironment(environment.entrySet().stream().map(it -> new AbstractMap.SimpleImmutableEntry<String, String>(it.getKey(), it.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        runner.withArguments(getAllArguments());

        return runner;
    }

    // TODO: This is not how we want to solve this use case!
    public void usingGradleVersion(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    private static class GradleRunnerExecutionResult implements ExecutionResult {
        private final BuildResult result;

        GradleRunnerExecutionResult(BuildResult result) {
            this.result = result;
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
        public String getOutput() {
            return result.getOutput();
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
            assert result.getOutput().contains(expectedOutput.trim());
            return this;
        }

        @Override
        public ExecutionResult assertHasPostBuildOutput(String expectedOutput) {
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
    }

    private static class GradleRunnerExecutionFailure extends GradleRunnerExecutionResult implements ExecutionFailure {
        GradleRunnerExecutionFailure(BuildResult result) {
            super(result);
        }
    }
}
