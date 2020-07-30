/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.Pair;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import junit.framework.AssertionFailedError;
import org.gradle.util.TextUtil;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class OutputScrapingExecutionFailure extends OutputScrapingExecutionResult implements ExecutionFailure {
    private static final Pattern FAILURE_PATTERN = Pattern.compile("FAILURE: (.+)");
    private static final Pattern CAUSE_PATTERN = Pattern.compile("(?m)(^\\s*> )");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("(?ms)^\\* What went wrong:$(.+?)^\\* Try:$");
    private static final Pattern LOCATION_PATTERN = Pattern.compile("(?ms)^\\* Where:((.+?)'.+?') line: (\\d+)$");
    private static final Pattern RESOLUTION_PATTERN = Pattern.compile("(?ms)^\\* Try:$(.+?)^\\* Exception is:$");
    private final String summary;
    private final List<Problem> problems = new ArrayList<>();
    private final List<Problem> problemsNotChecked = new ArrayList<>();
    private final List<String> lineNumbers = new ArrayList<>();
    private final List<String> fileNames = new ArrayList<>();
    private final String resolution;
    private final LogContent mainContent;

    static boolean hasFailure(String error) {
        return FAILURE_PATTERN.matcher(error).find();
    }

    /**
     * Creates a result from the output of a <em>single</em> Gradle invocation.
     *
     * @param output The raw build stdout chars.
     * @param error The raw build stderr chars.
     * @return A {@link OutputScrapingExecutionResult} for a successful build, or a {@link OutputScrapingExecutionFailure} for a failed build.
     */
    public static OutputScrapingExecutionFailure from(String output, String error) {
        return new OutputScrapingExecutionFailure(output, error, true);
    }

    protected OutputScrapingExecutionFailure(String output, String error, boolean includeBuildSrc) {
        super(LogContent.of(output), LogContent.of(error), includeBuildSrc);

        LogContent withoutDebug = LogContent.of(output).ansiCharsToPlainText().removeDebugPrefix();

        // Find failure section
        Pair<LogContent, LogContent> match = withoutDebug.splitOnFirstMatchingLine(FAILURE_PATTERN);
        if (match == null) {
            // Not present in output, check error output.
            match = LogContent.of(error).ansiCharsToPlainText().removeDebugPrefix().splitOnFirstMatchingLine(FAILURE_PATTERN);
            if (match != null) {
                match = Pair.of(withoutDebug, match.getRight());
            } else {
                // Not present, assume no failure details
                match = Pair.of(withoutDebug, LogContent.empty());
            }
        } else {
            if (match.getRight().countMatches(FAILURE_PATTERN) != 1) {
                throw new IllegalArgumentException("Found multiple failure sections in log output: " + output);
            }
        }

        LogContent failureContent = match.getRight();
        this.mainContent = match.getLeft();

        String failureText = failureContent.withNormalizedEol();

        java.util.regex.Matcher matcher = FAILURE_PATTERN.matcher(failureText);
        if (matcher.lookingAt()) {
            summary = matcher.group(1);
        } else {
            summary = "";
        }

        matcher = LOCATION_PATTERN.matcher(failureText);
        while (matcher.find()) {
            fileNames.add(matcher.group(1).trim());
            lineNumbers.add(matcher.group(3));
        }

        matcher = DESCRIPTION_PATTERN.matcher(failureText);
        while (matcher.find()) {
            String problemStr = matcher.group(1);
            Problem problem = extract(problemStr);
            problems.add(problem);
            problemsNotChecked.add(problem);
        }

        matcher = RESOLUTION_PATTERN.matcher(failureText);
        if (!matcher.find()) {
            resolution = "";
        } else {
            resolution = matcher.group(1).trim();
        }
    }

    @Override
    public ExecutionFailure getIgnoreBuildSrc() {
        return new OutputScrapingExecutionFailure(getOutput(), getError(), false);
    }

    @Override
    public LogContent getMainContent() {
        return mainContent;
    }

    private Problem extract(String problem) {
        java.util.regex.Matcher matcher = CAUSE_PATTERN.matcher(problem);
        String description;
        List<String> causes = new ArrayList<>();
        if (!matcher.find()) {
            description = TextUtil.normaliseLineSeparators(problem.trim());
        } else {
            description = TextUtil.normaliseLineSeparators(problem.substring(0, matcher.start()).trim());
            while (true) {
                int pos = matcher.end();
                int prefix = matcher.group(1).length();
                String prefixPattern = toPrefixPattern(prefix);
                if (matcher.find(pos)) {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos, matcher.start()).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                } else {
                    String cause = TextUtil.normaliseLineSeparators(problem.substring(pos).trim().replaceAll(prefixPattern, ""));
                    causes.add(cause);
                    break;
                }
            }
        }
        return new Problem(description, causes);
    }

    private String toPrefixPattern(int prefix) {
        StringBuilder builder = new StringBuilder("(?m)^");
        for (int i = 0; i < prefix; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }

    public ExecutionFailure assertHasLineNumber(int lineNumber) {
        assertThat(this.lineNumbers, hasItem(equalTo(String.valueOf(lineNumber))));
        return this;
    }

    public ExecutionFailure assertHasFileName(String filename) {
        assertThat(this.fileNames, hasItem(equalTo(filename)));
        return this;
    }

    public ExecutionFailure assertHasFailures(int count) {
        problemsNotChecked.clear(); // this is a good enough check for now
        assertThat(this.problems.size(), equalTo(count));
        if (count == 1) {
            MatcherAssert.assertThat(summary, equalTo("Build failed with an exception."));
        } else {
            assertThat(summary, equalTo(String.format("Build completed with %s failures.", count)));
        }
        return this;
    }

    @Override
    public ExecutionFailure assertHasCause(String description) {
        assertThatCause(startsWith(description));
        return this;
    }

    @Override
    public ExecutionFailure assertThatCause(Matcher<? super String> matcher) {
        Set<String> seen = new LinkedHashSet<>();
        for (Problem problem : problems) {
            for (String cause : problem.causes) {
                if (matcher.matches(cause)) {
                    problemsNotChecked.remove(problem);
                    return this;
                }
                seen.add(cause);
            }
        }
        failureOnUnexpectedOutput(String.format("No matching cause found in %s", seen));
        return this;
    }

    public ExecutionFailure assertHasResolution(String resolution) {
        assertThat(this.resolution, containsString(resolution));
        return this;
    }

    public ExecutionFailure assertHasNoCause(String description) {
        Matcher<String> matcher = containsString(description);
        for (Problem problem : problems) {
            for (String cause : problem.causes) {
                if (matcher.matches(cause)) {
                    failureOnUnexpectedOutput(String.format("Expected no failure with description '%s', found: %s", description, cause));
                }
            }
        }
        return this;
    }

    public ExecutionFailure assertHasNoCause() {
        for (Problem problem : problems) {
            if (!problem.causes.isEmpty()) {
                failureOnUnexpectedOutput(String.format("Expected no failure with a cause, found: %s", problem.causes.get(0)));
            }
        }
        return this;
    }

    @Override
    public ExecutionFailure assertHasDescription(String context) {
        assertThatDescription(startsWith(context));
        return this;
    }

    public ExecutionFailure assertThatDescription(Matcher<? super String> matcher) {
        assertHasFailure(matcher, f -> {
        });
        return this;
    }

    public ExecutionFailure assertHasFailure(String description, Consumer<? super Failure> action) {
        assertHasFailure(startsWith(description), action);
        return this;
    }

    private void assertHasFailure(Matcher<? super String> matcher, Consumer<? super Failure> action) {
        Set<String> seen = new LinkedHashSet<>();
        for (Problem problem : problems) {
            if (matcher.matches(problem.description)) {
                problemsNotChecked.remove(problem);
                action.accept(problem);
                return;
            }
            seen.add(problem.description);
        }
        failureOnUnexpectedOutput(String.format("No matching failure description found in %s", seen));
    }

    public ExecutionFailure assertTestsFailed() {
        new DetailedExecutionFailure(this).assertTestsFailed();
        return this;
    }

    public DependencyResolutionFailure assertResolutionFailure(String configurationPath) {
        return new DependencyResolutionFailure(this, configurationPath);
    }

    @Override
    public void assertResultVisited() {
        super.assertResultVisited();
        // Ensure that exceptions are not unintentionally introduced.
        if (problems.size() > 1 && !problemsNotChecked.isEmpty()) {
            throw new AssertionFailedError("The build failed with multiple exceptions, however not all exceptions where checked during the test. This can be done using assertHasFailures(n), assertHasDescription() or assertHasCause() or one of the variants of these methods.");
        }
    }

    private static class Problem implements Failure {
        final String description;
        final List<String> causes;

        private Problem(String description, List<String> causes) {
            this.description = description;
            this.causes = causes;
        }

        @Override
        public void assertHasCauses(int count) {
            assert causes.size() == count;
        }
    }
}
