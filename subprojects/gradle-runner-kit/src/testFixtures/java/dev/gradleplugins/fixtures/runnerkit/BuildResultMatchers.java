package dev.gradleplugins.fixtures.runnerkit;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.BuildTask;
import dev.gradleplugins.runnerkit.TaskOutcome;
import org.hamcrest.*;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class BuildResultMatchers {
    private BuildResultMatchers() {}

    public static Matcher<BuildResult> hasFailureCause(String cause) {
        return hasFailureCause(startsWith(cause));
    }

    public static Matcher<BuildResult> hasFailureCause(Matcher<? super String> matcher) {
        return new FailureCause(matcher);
    }

    private static final class FailureCause extends TypeSafeDiagnosingMatcher<BuildResult> {
        private final Matcher<? super String> matcher;

        FailureCause(Matcher<? super String> matcher) {
            this.matcher = matcher;
        }

        @Override
        protected boolean matchesSafely(BuildResult item, Description mismatchDescription) {
            for (BuildResult.Failure failure : item.getFailures()) {
                for (String cause : failure.getCauses()) {
                    if (matcher.matches(cause)) {
                        return true;
                    }
                }
            }
            mismatchDescription.appendValueList("none of the following causes matches: ", ", ", "", item.getFailures().stream().flatMap(it -> it.getCauses().stream()).distinct().collect(toList()));
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a failure cause matching ").appendDescriptionOf(matcher);
        }
    }

    public static Matcher<BuildResult> hasFailureDescription(String description) {
        return hasFailureDescription(startsWith(description));
    }

    public static Matcher<BuildResult> hasFailureDescription(Matcher<? super String> matcher) {
        return new FailureDescription(matcher);
    }

    private static final class FailureDescription extends TypeSafeDiagnosingMatcher<BuildResult> {
        private final Matcher<? super String> matcher;

        FailureDescription(Matcher<? super String> matcher) {
            this.matcher = matcher;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("a failure description matching ").appendDescriptionOf(matcher);
        }

        @Override
        protected boolean matchesSafely(BuildResult item, Description mismatchDescription) {
            for (BuildResult.Failure failure : item.getFailures()) {
                if (matcher.matches(failure.getDescription())) {
                    return true;
                }
            }
            mismatchDescription.appendValueList("none of the following description matches: ", ", ", "", item.getFailures().stream().map(BuildResult.Failure::getDescription).distinct().collect(toList()));
            return false;
        }
    }

    private abstract static class AbstractTaskMatcher extends TypeSafeDiagnosingMatcher<BuildResult> {
        private final String description;
        private final Function<BuildResult, List<String>> extractTaskPaths;
        private final Matcher<Iterable<? extends String>> matcher;

        AbstractTaskMatcher(String description, Function<BuildResult, List<String>> extractTaskPaths, List<String> expectedTasks) {
            assertThat("all expected task paths must be valid task paths", expectedTasks, Matchers.everyItem(startsWith(":")));
            this.description = description;
            this.extractTaskPaths = extractTaskPaths;
            this.matcher = containsInAnyOrder(expectedTasks.stream().map(Matchers::equalTo).collect(toList()));
        }

        @Override
        protected boolean matchesSafely(BuildResult item, Description mismatchDescription) {
            List<String> actualTasks = extractTaskPaths.apply(item);
            if (!matcher.matches(actualTasks)) {
                matcher.describeMismatch(actualTasks, mismatchDescription);
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(this.description).appendDescriptionOf(matcher);
        }
    }

    public static Matcher<BuildResult> tasksExecuted(Object... taskPaths) {
        return new TasksExecuted(flattenTaskPaths(taskPaths));
    }

    private static final class TasksExecuted extends AbstractTaskMatcher {
        TasksExecuted(List<String> taskPaths) {
            super("executed task paths of ", BuildResult::getExecutedTaskPaths, taskPaths);
        }
    }

    public static Matcher<BuildResult> tasksSkipped(Object... taskPaths) {
        return new TasksSkipped(flattenTaskPaths(taskPaths));
    }

    private static final class TasksSkipped extends AbstractTaskMatcher {
        TasksSkipped(List<String> taskPaths) {
            super("skipped task paths of ", BuildResult::getSkippedTaskPaths, taskPaths);
        }
    }

    public static Matcher<BuildResult> tasksExecutedAndNotSkipped(Object... taskPaths) {
        return new TasksExecutedAndNotSkipped(flattenTaskPaths(taskPaths));
    }

    private static final class TasksExecutedAndNotSkipped extends AbstractTaskMatcher {
        TasksExecutedAndNotSkipped(List<String> taskPaths) {
            super("executed and not skipped task paths of ", TasksExecutedAndNotSkipped::getExecutedAndNotSkippedTaskPaths, taskPaths);
        }
        private static List<String> getExecutedAndNotSkippedTaskPaths(BuildResult item) {
            return item.getTasks().stream().filter(TasksExecutedAndNotSkipped::isExecutedAndNotSkipped).map(BuildTask::getPath).collect(toList());
        }

        private static boolean isExecutedAndNotSkipped(BuildTask buildTask) {
            return buildTask.getOutcome().equals(TaskOutcome.SUCCESS) || buildTask.getOutcome().equals(TaskOutcome.FAILED);
        }
    }

    public static List<String> flattenTaskPaths(Object[] taskPaths) {
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
}
