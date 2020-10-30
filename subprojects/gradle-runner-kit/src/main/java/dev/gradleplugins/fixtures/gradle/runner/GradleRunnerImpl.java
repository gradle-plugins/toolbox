package dev.gradleplugins.fixtures.gradle.runner;

import dev.gradleplugins.fixtures.gradle.runner.parameters.*;
import lombok.val;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

final class GradleRunnerImpl implements GradleRunner {
    private final GradleExecutor executor;
    private final GradleRunnerParameters parameters;

    public GradleRunnerImpl(GradleExecutor executor) {
        this(executor, new GradleRunnerParameters(toExecutorType(executor)));
    }

    private static GradleExecutorType toExecutorType(GradleExecutor executor) {
        if (executor instanceof GradleExecutorGradleTestKitImpl) {
            return GradleExecutorType.GRADLE_TEST_KIT;
        } else if (executor instanceof GradleExecutorGradleWrapperImpl) {
            return GradleExecutorType.GRADLE_WRAPPER;
        }
        return GradleExecutorType.UNKNOWN;
    }

    private GradleRunnerImpl(GradleExecutor executor, GradleRunnerParameters parameters) {
        this.executor = executor;
        this.parameters = parameters;
    }

    private GradleRunnerImpl newInstance(GradleRunnerParameters parameters) {
        return new GradleRunnerImpl(executor, parameters);
    }

    //region Working directory configuration
    @Override
    public GradleRunner inDirectory(File workingDirectory) {
        return newInstance(parameters.withWorkingDirectory(WorkingDirectory.of(workingDirectory)));
    }
    //endregion

    //region Flag `--stack-trace` configuration
    @Override
    public GradleRunner withStacktraceDisabled() {
        return newInstance(parameters.withStacktrace(Stacktrace.HIDE));
    }
    //endregion

    //region Flag `--build-cache` configuration
    @Override
    public GradleRunner withBuildCacheEnabled() {
        return newInstance(parameters.withBuildCache(BuildCache.ENABLED));
    }
    //endregion

    //region Gradle tasks configuration
    @Override
    public GradleRunner withTasks(List<String> tasks) {
        return newInstance(parameters.withTasks(parameters.getTasks().plus(tasks)));
    }
    //endregion

    //region Process arguments configuration
    @Override
    public GradleRunner withArguments(List<String> args) {
        return newInstance(parameters.withArguments(CommandLineArguments.of(args)));
    }

    @Override
    public GradleRunner withArgument(String arg) {
        return newInstance(parameters.withArguments(parameters.getArguments().plus(arg)));
    }

    @Override
    public List<String> getAllArguments() {
        return parameters.getAllArguments();
    }
    //endregion

    //region Settings file configuration
    @Override
    public GradleRunner usingSettingsFile(File settingsFile) {
        return newInstance(parameters.withSettingsFile(SettingsFile.of(settingsFile)));
    }

    @Override
    public GradleRunner ignoresMissingSettingsFile() {
        return newInstance(parameters.withMissingSettingsFilePolicy(MissingSettingsFilePolicy.IGNORES_WHEN_MISSING));
    }
    //endregion

    //region Flag `--build-file` configuration
    @Override
    public GradleRunner usingBuildScript(File buildScript) {
        return newInstance(parameters.withBuildScript(BuildScript.of(buildScript)));
    }
    //endregion

    //region Flag `--init-script` configuration
    @Override
    public GradleRunner usingInitScript(File initScript) {
        return newInstance(parameters.withInitScripts(parameters.getInitScripts().plus(initScript)));
    }
    //endregion

    //region Flag `--project-dir` configuration
    @Override
    public GradleRunner usingProjectDirectory(File projectDirectory) {
        return newInstance(parameters.withProjectDirectory(ProjectDirectory.of(projectDirectory)));
    }
    //endregion

    //region Deprecation warning checks configuration
    @Override
    public GradleRunner withoutDeprecationChecks() {
        return newInstance(parameters.withDeprecationChecks(DeprecationChecks.IGNORES));
    }
    //endregion

    //region Default character encoding configuration
    @Override
    public GradleRunner withDefaultCharacterEncoding(Charset defaultCharacterEncoding) {
        return newInstance(parameters.withDefaultCharacterEncoding(CharacterEncoding.of(defaultCharacterEncoding)));
    }
    //endregion

    //region Default locale configuration
    @Override
    public GradleRunner withDefaultLocale(java.util.Locale defaultLocale) {
        return newInstance(parameters.withDefaultLocale(Locale.of(defaultLocale)));
    }
    //endregion

    //region Welcome message configuration
    @Override
    public GradleRunner withWelcomeMessageEnabled() {
        return newInstance(parameters.withWelcomeMessageRendering(WelcomeMessage.ENABLED));
    }
    //endregion

    //region Build scan configuration
    @Override
    public GradleRunner publishBuildScans() {
        return newInstance(parameters.withBuildScan(BuildScan.ENABLED));
    }
    //endregion

    //region Flag `-Djava.home` configuration
    @Override
    public GradleRunner withUserHomeDirectory(File userHomeDirectory) {
        return newInstance(parameters.withUserHomeDirectory(UserHomeDirectory.of(userHomeDirectory)));
    }
    //endregion

    //region Flag `--gradle-user-home` configuration
    @Override
    public GradleRunner withGradleUserHomeDirectory(File gradleUserHomeDirectory) {
        return newInstance(parameters.withGradleUserHomeDirectory(GradleUserHomeDirectory.of(gradleUserHomeDirectory)));
    }

//    @Override
//    public GradleRunner requireOwnGradleUserHomeDirectory() {
//        return newInstance(configuration.withGradleUserHomeDirectory(GradleUserHomeDirectoryParameter.of(GradleUserHomeDirectory.of(testDirectory.createDirectory("user-home")))));
//    }
    //endregion

    //region Environment variables configuration
    @Override
    public GradleRunner withEnvironmentVariables(Map<String, ?> environment) {
        return newInstance(parameters.withEnvironmentVariables(parameters.getEnvironmentVariables().plus(environment)));
    }
    //endregion

    //region Rich console configuration
    @Override
    public GradleRunner withRichConsoleEnabled() {
        return newInstance(parameters.withConsoleType(ConsoleType.RICH));
    }
    //endregion

    //region Standard output configuration
    @Override
    public GradleRunner forwardStandardOutput(Writer writer) {
        return newInstance(parameters.withStandardOutput(StandardOutput.of(writer)));
    }

    @Override
    public GradleRunner forwardStandardError(Writer writer) {
        return newInstance(parameters.withStandardError(StandardOutput.of(writer)));
    }

    @Override
    public GradleRunner forwardOutput() {
        return newInstance(parameters.withStandardOutput(StandardOutput.forwardToStandardOutput()).withStandardError(StandardOutput.forwardToStandardOutput()));
    }
    //endregion

    @Override
    public GradleBuildResult build() {
        val gradleExecutionResult = executor.run(parameters.calculateValues());
        val result = GradleBuildResult.from(gradleExecutionResult.getOutput());
        if (!gradleExecutionResult.isSuccessful()) {
            throw new UnexpectedBuildFailure(createDiagnosticsMessage("Unexpected build execution failure", gradleExecutionResult), result);
        }
        return result;
    }

    @Override
    public GradleBuildResult buildAndFail() {
        val gradleExecutionResult = executor.run(parameters.calculateValues());
        val result = GradleBuildResult.from(gradleExecutionResult.getOutput());
        if (gradleExecutionResult.isSuccessful()) {
            throw new UnexpectedBuildSuccess(createDiagnosticsMessage("Unexpected build execution success", gradleExecutionResult), result);
        }
        return result;
    }

    String createDiagnosticsMessage(String trailingMessage, GradleExecutionResult gradleExecutionResult) {
        String lineBreak = System.lineSeparator();
        StringBuilder message = new StringBuilder();
        message.append(trailingMessage);
        message.append(" in ");
        message.append(parameters.getProjectDirectory().orElseGet(parameters.getWorkingDirectory()::get).getAbsolutePath());
        message.append(" with arguments ");
        message.append(parameters.getArguments().get()); // TODO: Should we just use all arguments

        String output = gradleExecutionResult.getOutput();
        if (output != null && !output.isEmpty()) {
            message.append(lineBreak);
            message.append(lineBreak);
            message.append("Output:");
            message.append(lineBreak);
            message.append(output);
        }

        return message.toString();
    }
}
