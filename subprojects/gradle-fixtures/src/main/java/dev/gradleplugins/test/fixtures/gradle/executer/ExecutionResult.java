package dev.gradleplugins.test.fixtures.gradle.executer;

import org.hamcrest.Matcher;

public interface ExecutionResult {
    /**
     * Stdout of the Gradle execution, normalized to use new-line char as line separator.
     *
     * <p>You should avoid using this method as it couples the tests to a particular layout for the console. Instead use the more descriptive assertion methods on this class.</p>
     */
    String getOutput();

    /**
     * Stdout of the Gradle execution, with ANSI characters interpreted and text attributes discarded.
     */
    String getPlainTextOutput();

    /**
     * Asserts that the given task has not been executed.
     */
    ExecutionResult assertTaskNotExecuted(String taskPath);

    /**
     * Asserts that the given task has not been skipped.
     */
    ExecutionResult assertTaskNotSkipped(String taskPath);

    /**
     * Asserts the given task has been skipped.
     */
    ExecutionResult assertTaskSkipped(String taskPath);

    /**
     * Asserts that exactly the given set of tasks have been executed in any order and none of the tasks were skipped.
     */
    ExecutionResult assertTasksExecutedAndNotSkipped(Object... taskPaths);

    /**
     * Asserts that exactly the given set of tasks have been executed in any order.
     */
    ExecutionResult assertTasksExecuted(Object... taskPaths);

    /**
     * Asserts that exactly the given set of tasks have been skipped.
     */
    ExecutionResult assertTasksSkipped(Object... taskPaths);

    /**
     * Asserts that exactly the given set of tasks have not been skipped.
     */
    ExecutionResult assertTasksNotSkipped(Object... taskPaths);

    /**
     * Asserts that this result includes the given non-error log message. Does not consider any text in or following the build result message (use {@link #assertHasPostBuildOutput(String)} instead).
     *
     * @param expectedOutput The expected log message, with line endings normalized to a newline character.
     */
    ExecutionResult assertOutputContains(String expectedOutput);

    /**
     * Asserts that the non-error log message matches.
     *
     * <p>Log messages are normalized to use new-line char as line separator.
     *
     * @param matcher the matcher to use
     */
    ExecutionResult assertThatOutput(Matcher<? super String> matcher);

    /**
     * Asserts that this result does not include the given log message anywhere in the build output.
     *
     * @param expectedOutput The expected log message, with line endings normalized to a newline character.
     */
    ExecutionResult assertNotOutput(String expectedOutput);

    /**
     * Assert that the given message appears after the build result message.
     *
     * @param expectedOutput The expected log message, with line endings normalized to a newline character.
     */
    ExecutionResult assertHasPostBuildOutput(String expectedOutput);

    /**
     * Asserts that the important information from this result has been verified by the test.
     */
    void assertResultVisited();
}
