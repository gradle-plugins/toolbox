package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import org.hamcrest.Matcher;

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class ExecutionResultImpl implements ExecutionResult, ExecutionFailure {
    public final BuildResult delegate;

    public ExecutionResultImpl(BuildResult delegate) {
        this.delegate = delegate;
    }

    @Override
    public ExecutionFailure assertHasCause(String description) {
        assertThat(delegate, hasFailureCause(description));
        return this;
    }

    @Override
    public ExecutionFailure assertThatCause(Matcher<? super String> matcher) {
        assertThat(delegate, hasFailureCause(matcher));
        return this;
    }

    @Override
    public ExecutionFailure assertHasDescription(String context) {
        assertThat(delegate, hasFailureDescription(context));
        return this;
    }

    @Override
    public String getOutput() {
        return delegate.getOutput();
    }

//    @Override
//    public GroupedOutputFixture getGroupedOutput() {
//        throw new UnsupportedOperationException("Not implemented");
//    }

    @Override
    public String getPlainTextOutput() {
        return delegate.getOutput();
    }

    @Override
    public ExecutionResult assertTaskNotExecuted(String taskPath) {
        assertThat("Build output does contains unexpected task.", delegate.getExecutedTaskPaths(), not(hasItem(taskPath)));
        return this;
    }

    @Override
    public ExecutionResult assertTaskNotSkipped(String taskPath) {
        assertThat("Build output does not contain the expected non skipped task.", delegate.getSkippedTaskPaths(), not(hasItem(taskPath)));
        return this;
    }

    @Override
    public ExecutionResult assertTaskSkipped(String taskPath) {
        assertThat("Build output does not contain the expected skipped task.", delegate.getSkippedTaskPaths(), hasItem(taskPath));
        return this;
    }

    @Override
    public ExecutionResult assertTasksExecutedAndNotSkipped(Object... taskPaths) {
        assertThat(delegate, tasksExecutedAndNotSkipped(taskPaths));
        return this;
    }

    @Override
    public ExecutionResult assertTasksExecuted(Object... taskPaths) {
        assertThat(delegate, tasksExecuted(taskPaths));
        return this;
    }

    @Override
    public ExecutionResult assertTasksSkipped(Object... taskPaths) {
        assertThat(delegate, tasksSkipped(taskPaths));
        return this;
    }

    @Override
    public ExecutionResult assertTasksNotSkipped(Object... taskPaths) {
        assertThat(delegate, not(tasksSkipped(taskPaths)));
        return this;
    }

    @Override
    public ExecutionResult assertOutputContains(String expectedOutput) {
        assertThat(delegate.getOutput(), containsString(expectedOutput));
        return this;
    }

    @Override
    public ExecutionResult assertThatOutput(Matcher<? super String> matcher) {
        assertThat(delegate.getOutput(), matcher);
        return this;
    }

    @Override
    public ExecutionResult assertNotOutput(String expectedOutput) {
        assertThat(delegate.getOutput(), not(containsString(expectedOutput)));
        return this;
    }

    @Override
    public ExecutionResult assertHasPostBuildOutput(String expectedOutput) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void assertResultVisited() {

    }
}
