package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.ActionableTaskCount;
import dev.gradleplugins.runnerkit.BuildOutcome;
import dev.gradleplugins.runnerkit.CommandLineToolLogContent;
import dev.gradleplugins.runnerkit.TaskOutcomeUtils;
import dev.gradleplugins.runnerkit.TaskPath;
import lombok.val;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class CommandLineToolLogContentLineVisitorAdapter implements Consumer<CommandLineToolLogContent.LineDetails> {
    //for example: ':hey' or ':a SKIPPED' or ':foo:bar:baz UP-TO-DATE' but not ':a FOO'
    private static final Pattern TASK_PATTERN = Pattern.compile("(> Task )?(:\\S+)\\s*(SKIPPED|UP-TO-DATE|FROM-CACHE|NO-SOURCE|FAILED)?");
    private static final Pattern ACTIONABLE_TASKS_PATTERN = Pattern.compile("(\\d+) actionable tasks?: (\\d+) executed(, (\\d+) up-to-date)?");

    private final BuildLogVisitor visitor;

    public CommandLineToolLogContentLineVisitorAdapter(BuildLogVisitor visitor) {
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
                visitor.visitTaskHeader(TaskPath.of(taskPath), TaskOutcomeUtils.from(taskOutcome));
            }
        } else if (details.getLine().startsWith("FAILURE: ")) {
            visitor.visitBuildFailure();
        } else if (details.getLine().equals("BUILD SUCCESSFUL")) {
            visitor.visitBuildResult(BuildOutcome.SUCCESSFUL);
        } else if (details.getLine().equals("BUILD FAILED")) {
            visitor.visitBuildResult(BuildOutcome.FAILED);
        } else if (ACTIONABLE_TASKS_PATTERN.matcher(details.getLine()).matches()) {
            val m = ACTIONABLE_TASKS_PATTERN.matcher(details.getLine());
            m.matches();
            val actionableTaskCount = Integer.parseInt(m.group(1));
            val executedTaskCount = Integer.parseInt(m.group(2));
            val upToDateTaskCount = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            assert actionableTaskCount == (executedTaskCount + upToDateTaskCount);
            visitor.visitActionableTasks(ActionableTaskCount.of(executedTaskCount, upToDateTaskCount));
        } else {
            visitor.visitContentLine(details.getLine());
        }
    }
}