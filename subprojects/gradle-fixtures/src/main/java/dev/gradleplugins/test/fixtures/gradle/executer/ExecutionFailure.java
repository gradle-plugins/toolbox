package dev.gradleplugins.test.fixtures.gradle.executer;

import org.hamcrest.Matcher;

public interface ExecutionFailure extends ExecutionResult {
    /**
     * Asserts that the reported failure has the given cause (ie the bit after the description).
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertHasCause(String description);

    /**
     * Asserts that the reported failure has the given cause (ie the bit after the description).
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertThatCause(Matcher<? super String> matcher);

    /**
     * Asserts that the reported failure has the given description (ie the bit after '* What went wrong').
     *
     * <p>Error messages are normalized to use new-line char as line separator.
     */
    ExecutionFailure assertHasDescription(String context);
}
