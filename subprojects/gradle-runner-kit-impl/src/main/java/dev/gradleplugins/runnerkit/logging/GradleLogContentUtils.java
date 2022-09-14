package dev.gradleplugins.runnerkit.logging;

import dev.gradleplugins.runnerkit.ActionableTaskCount;
import dev.gradleplugins.runnerkit.BuildFailures;
import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.BuildResultImpl;
import dev.gradleplugins.runnerkit.CommandLineToolLogContent;
import lombok.val;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class GradleLogContentUtils {
    private GradleLogContentUtils() {}

    public static Consumer<CommandLineToolLogContent.LineDetails> normalizedBuildResult() {
        return BuildResultNormalizer.INSTANCE;
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

    public static Consumer<CommandLineToolLogContent.LineDetails> removedWarningSummaryMessage() {
        return WarningSummaryMessageNormalizer.INSTANCE;
    }

    private enum WarningSummaryMessageNormalizer implements Consumer<CommandLineToolLogContent.LineDetails> {
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

    public static Consumer<CommandLineToolLogContent.LineDetails> removedDaemonMessage() {
        return GradleDaemonMessageNormalizer.INSTANCE;
    }

    private enum GradleDaemonMessageNormalizer implements Consumer<CommandLineToolLogContent.LineDetails> {
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

    public static Consumer<CommandLineToolLogContent.LineDetails> removedJavaIllegalAccessWarnings() {
        return JavaIllegalAccessWarningsNormalizer.INSTANCE;
    }

    private enum JavaIllegalAccessWarningsNormalizer implements Consumer<CommandLineToolLogContent.LineDetails> {
        INSTANCE;

        @Override
        public void accept(CommandLineToolLogContent.LineDetails details) {
            if (details.getLine().startsWith("WARNING: An illegal reflective access operation has occurred") || details.getLine().equals("WARNING: All illegal access operations will be denied in a future release")) {
                details.dropLine();
            }
        }
    }

    public static CommandLineToolLogContent normalize(CommandLineToolLogContent content) {
        return content
                .withAnsiControlCharactersInterpreted()
                .withNormalizedEndOfLine()
                .visitEachLine(removedJavaIllegalAccessWarnings())
                .visitEachLine(removedDaemonMessage())
                .visitEachLine(removedWarningSummaryMessage())
                .visitEachLine(normalizedBuildResult());
    }

    public static BuildResult scrapBuildResultFrom(String output) {
        return scrapBuildResultFrom(CommandLineToolLogContent.of(output));
    }

    public static BuildResult scrapBuildResultFrom(CommandLineToolLogContent output) {
        val normalizedOutput = normalize(output);

        val visitor = new OutputScrappingBuildLogVisitor();
        normalizedOutput.visitEachLine(new CommandLineToolLogContentLineVisitorAdapter(new TaskOutputGroupingVisitor(visitor)));

        val discoveredTasks = visitor.getDiscoveredTasks();
        val actionableTaskCount = visitor.getActionableTaskCount().orElseGet(() -> ActionableTaskCount.from(discoveredTasks));

        return new BuildResultImpl(discoveredTasks, normalizedOutput, actionableTaskCount, visitor.getBuildOutcome(), BuildFailures.from(normalizedOutput));
    }
}
