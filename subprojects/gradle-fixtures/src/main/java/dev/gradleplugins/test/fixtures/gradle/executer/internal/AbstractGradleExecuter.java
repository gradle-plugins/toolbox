package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.daemon.DaemonLogsAnalyzer;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters.*;
import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput;
import dev.gradleplugins.test.fixtures.scan.GradleEnterpriseBuildScan;
import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public abstract class AbstractGradleExecuter implements GradleExecuter {
    protected GradleExecutionParameters configuration;
    private final TestFile testDirectory;

    public AbstractGradleExecuter(@NonNull GradleDistribution distribution, @NonNull TestFile testDirectory, @NonNull GradleExecuterBuildContext buildContext) {
        this(testDirectory, new GradleExecutionParameters(distribution, buildContext).withGradleUserHomeDirectory(GradleUserHomeDirectoryParameter.of(GradleUserHomeDirectory.of(buildContext.getGradleUserHomeDirectory()))).withDaemonBaseDirectory(DaemonBaseDirectoryParameter.of(DaemonBaseDirectory.of(buildContext.getDaemonBaseDirectory()))));
    }

    protected AbstractGradleExecuter(TestFile testDirectory, GradleExecutionParameters configuration) {
        this.configuration = configuration;
        this.testDirectory = testDirectory;
    }

    protected GradleExecuter newInstance(GradleExecutionParameters configuration) {
        return newInstance(testDirectory, configuration);
    }

    protected abstract GradleExecuter newInstance(TestFile testDirectory, GradleExecutionParameters configuration);

    @Override
    public GradleDistribution getDistribution() {
        return configuration.getDistribution();
    }

    @Override
    public TestFile getTestDirectory() {
        return testDirectory;
    }

    //region Working directory configuration
    public File getWorkingDirectory() {
        return configuration.getWorkingDirectory().orElse(WorkingDirectory.of(testDirectory)).getAsFile();
    }

    @Override
    public GradleExecuter inDirectory(File directory) {
        return newInstance(configuration.withWorkingDirectory(WorkingDirectoryParameter.of(WorkingDirectory.of(directory))));
    }
    //endregion

    //region Flag `-Djava.home` configuration
    @Override
    public GradleExecuter withUserHomeDirectory(File userHomeDirectory) {
        return newInstance(configuration.withUserHomeDirectory(UserHomeDirectoryParameter.of(UserHomeDirectory.of(userHomeDirectory))));
    }
    //endregion

    //region Flag `--gradle-user-home` configuration
    @Override
    public GradleExecuter withGradleUserHomeDirectory(File gradleUserHomeDirectory) {
        return newInstance(configuration.withGradleUserHomeDirectory(GradleUserHomeDirectoryParameter.of(GradleUserHomeDirectory.of(gradleUserHomeDirectory))));
    }

    @Override
    public GradleExecuter requireOwnGradleUserHomeDirectory() {
        return newInstance(configuration.withGradleUserHomeDirectory(GradleUserHomeDirectoryParameter.of(GradleUserHomeDirectory.of(testDirectory.createDirectory("user-home")))));
    }
    //endregion

    //region Flag `--stack-trace` configuration
    @Override
    public GradleExecuter withStacktraceDisabled() {
        return newInstance(configuration.withShowStacktrace(StacktraceParameter.hide()));
    }
    //endregion

    //region Settings file configuration
    @Override
    public GradleExecuter usingSettingsFile(File settingsFile) {
        return newInstance(configuration.withSettingsFile(SettingsFileParameter.of(settingsFile)));
    }

    @Override
    public GradleExecuter ignoresMissingSettingsFile() {
        return newInstance(configuration.withMissingSettingsFilePolicy(MissingSettingsFilePolicy.ignores()));
    }
    //endregion

    //region Flag `--build-file` configuration
    @Override
    public GradleExecuter usingBuildScript(File buildScript) {
        return newInstance(configuration.withBuildScript(BuildScriptParameter.of(buildScript)));
    }
    //endregion

    //region Flag `--init-script` configuration
    @Override
    public GradleExecuter usingInitScript(File initScript) {
        return newInstance(configuration.withInitScripts(configuration.getInitScripts().plus(() -> initScript)));
    }
    //endregion

    //region Flag `--project-dir` configuration
    @Override
    public GradleExecuter usingProjectDirectory(File projectDirectory) {
        return newInstance(configuration.withProjectDirectory(ProjectDirectoryParameter.of(ProjectDirectory.of(projectDirectory))));
    }
    //endregion

    //region Flag `--build-cache` configuration
    @Override
    public GradleExecuter withBuildCacheEnabled() {
        return withArgument("--build-cache");
    }
    //endregion

    //region Process arguments configuration
    @Override
    public GradleExecuter withArguments(String... args) {
        return withArguments(asList(args));
    }

    @Override
    public GradleExecuter withArguments(List<String> args) {
        return newInstance(configuration.withArguments(args));
    }

    @Override
    public GradleExecuter withArgument(String arg) {
        return newInstance(configuration.withArguments(ImmutableList.<String>builder().addAll(configuration.getArguments()).add(arg).build()));
    }
    //endregion

    //region Gradle tasks configuration
    @Override
    public GradleExecuter withTasks(String... tasks) {
        return withTasks(asList(tasks));
    }

    @Override
    public GradleExecuter withTasks(List<String> tasks) {
        return newInstance(configuration.withTasks(ImmutableList.<String>builder().addAll(configuration.getTasks()).addAll(tasks).build()));
    }
    //endregion

    //region Before execute actions
    @Override
    public GradleExecuter beforeExecute(Function<? super GradleExecuter, GradleExecuter> action) {
        return newInstance(configuration.withBeforeExecute(ImmutableList.<Function<? super GradleExecuter, GradleExecuter>>builder().addAll(configuration.getBeforeExecute()).add(action).build()));
    }

    private GradleExecuter fireBeforeExecute() {
        GradleExecuter executer = newInstance(configuration.withBeforeExecute(ImmutableList.of()));
        for (val it : configuration.getBeforeExecute()) {
            executer = it.apply(executer);
        }
        return executer;
    }
    //endregion

    //region After execute actions
    @Override
    public GradleExecuter afterExecute(Consumer<? super GradleExecuter> action) {
        return newInstance(configuration.withAfterExecute(ImmutableList.<Consumer<? super GradleExecuter>>builder().addAll(configuration.getAfterExecute()).add(action).build()));
    }

    private void fireAfterExecute() {
        configuration.getAfterExecute().forEach(it -> it.accept(this));
    }
    //endregion

    //region Console type configuration
    @Override
    public GradleExecuter withConsole(ConsoleOutput consoleType) {
        return newInstance(configuration.withConsoleType(ConsoleTypeParameter.of(consoleType)));
    }
    //endregion

    //region Environment variables configuration
    @Override
    public GradleExecuter withEnvironmentVars(Map<String, ?> environment) {
        return newInstance(configuration.withEnvironment(configuration.getEnvironment().plus(environment)));
    }
    //endregion

    //region Deprecation warning checks configuration
    @Override
    public GradleExecuter withoutDeprecationChecks() {
        return newInstance(configuration.withDeprecationChecks(DeprecationChecksParameter.ignores()));
    }
    //endregion

    //region Daemon configuration
    @Override
    public GradleExecuter requireIsolatedDaemons() {
        return withDaemonBaseDirectory(getTestDirectory().file("daemon"));
    }

    @Override
    public GradleExecuter withDaemonBaseDirectory(File daemonBaseDirectory) {
        return newInstance(configuration.withDaemonBaseDirectory(DaemonBaseDirectoryParameter.of(DaemonBaseDirectory.of(daemonBaseDirectory))));
    }

    @Override
    public GradleExecuter withDaemonIdleTimeout(Duration daemonIdleTimeout) {
        return newInstance(configuration.withDaemonIdleTimeout(DaemonIdleTimeoutParameter.of(daemonIdleTimeout)));
    }

    @Override
    public GradleExecuter requireDaemon() {
        return newInstance(configuration.withRequireDaemon(true));
    }

    @Override
    public boolean usesDaemon() {
        CliDaemonArgument cliDaemonArgument = resolveCliDaemonArgument();
        if (cliDaemonArgument == CliDaemonArgument.NO_DAEMON || cliDaemonArgument == CliDaemonArgument.FOREGROUND) {
            return false;
        }
        return configuration.isRequireDaemon() || cliDaemonArgument == CliDaemonArgument.DAEMON;
    }

    enum CliDaemonArgument {
        NOT_DEFINED,
        DAEMON,
        NO_DAEMON,
        FOREGROUND
    }

    protected CliDaemonArgument resolveCliDaemonArgument() {
        for (int i = configuration.getArguments().size() - 1; i >= 0; i--) {
            final String arg = configuration.getArguments().get(i);
            if (arg.equals("--daemon")) {
                return CliDaemonArgument.DAEMON;
            }
            if (arg.equals("--no-daemon")) {
                return CliDaemonArgument.NO_DAEMON;
            }
            if (arg.equals("--foreground")) {
                return CliDaemonArgument.FOREGROUND;
            }
        }
        return CliDaemonArgument.NOT_DEFINED;
    }

    private boolean noDaemonArgumentGiven() {
        return resolveCliDaemonArgument() == CliDaemonArgument.NOT_DEFINED;
    }

    @Override
    public boolean usesSharedDaemons() {
        return isSharedDaemons();
    }

    protected boolean isSharedDaemons() {
        return configuration.getDaemonBaseDirectory().get().getAsFile().equals(configuration.getBuildContext().getDaemonBaseDirectory());
    }

    @Override
    public GradleExecuter withoutDaemonCrashChecks() {
        return newInstance(configuration.withDaemonCrashChecks(false));
    }

    /**
     * Performs cleanup at completion of the test.
     */
    public void cleanup() {
        cleanupIsolatedDaemons();
        for (ExecutionResult result : configuration.getExecutionResults()) {
            result.assertResultVisited();
        }
    }

    private void cleanupIsolatedDaemons() {
        List<DaemonLogsAnalyzer> analyzers = new ArrayList<>();
        for (File directory : configuration.getIsolatedDaemonBaseDirectories()) {
            try {
                DaemonLogsAnalyzer analyzer = new DaemonLogsAnalyzer(directory, configuration.getDistribution().getVersion().getVersion());
                analyzers.add(analyzer);
                analyzer.killAll();
            } catch (Exception e) {
                System.out.println("Problem killing isolated daemons of Gradle version " + configuration.getDistribution().getVersion().getVersion() + " in " + directory);
                e.printStackTrace();
            }
        }

        if (configuration.isDaemonCrashChecks()) {
            analyzers.forEach(DaemonLogsAnalyzer::assertNoCrashedDaemon);
        }
    }
    //endregion

    //region Temporary directory configuration
    @Override
    public GradleExecuter withoutExplicitTemporaryDirectory() {
        return newInstance(configuration.withTemporaryDirectory(TemporaryDirectoryParameter.explicit(TemporaryDirectory.of(configuration.getBuildContext().getTemporaryDirectory()))));
    }
    //endregion

    //region Default character encoding configuration
    @Override
    public GradleExecuter withDefaultCharacterEncoding(Charset defaultCharacterEncoding) {
        return newInstance(configuration.withDefaultCharacterEncoding(CharacterEncodingParameter.of(defaultCharacterEncoding)));
    }
    //endregion

    //region Default locale configuration
    @Override
    public GradleExecuter withDefaultLocale(Locale defaultLocale) {
        return newInstance(configuration.withDefaultLocale(LocaleParameter.of(defaultLocale)));
    }
    //endregion

    //region Welcome message configuration
    @Override
    public GradleExecuter withWelcomeMessageEnabled() {
        return newInstance(configuration.withRenderWelcomeMessage(WelcomeMessageParameter.enabled()));
    }
    //endregion

    //region Build scan configuration
    @Override
    public GradleExecuter withBuildScanEnabled() {
        return new GradleEnterpriseBuildScan().apply(this);
    }
    //endregion

    @Override
    public ExecutionResult run() {
        beforeBuildSetup();
        if (configuration.getBeforeExecute().isEmpty()) {
            try {
                ExecutionResult result = doRun();
                afterBuildCleanup(result);
                return result;
            } finally {
                finished();
            }
        } else {
            return fireBeforeExecute().run();
        }
    }

    private void afterBuildCleanup(ExecutionResult result) {
        fireAfterExecute();
        configuration = configuration.withExecutionResults(ImmutableList.<ExecutionResult>builder().addAll(configuration.getExecutionResults()).add(result).build());
    }

    protected abstract ExecutionResult doRun();

    @Override
    public ExecutionFailure runWithFailure() {
        beforeBuildSetup();
        if (configuration.getBeforeExecute().isEmpty()) {
            try {
                ExecutionFailure result = doRunWithFailure();
                afterBuildCleanup(result);
                return result;
            } finally {
                finished();
            }
        } else {
            return fireBeforeExecute().runWithFailure();
        }
    }

    private void beforeBuildSetup() {
        collectStateBeforeExecution();
    }

    private void collectStateBeforeExecution() {
        if (!isSharedDaemons()) {
            configuration = configuration.withIsolatedDaemonBaseDirectories(ImmutableList.<File>builder().addAll(configuration.getIsolatedDaemonBaseDirectories()).add(configuration.getDaemonBaseDirectory().get().getAsFile()).build());
        }
    }

    protected abstract ExecutionFailure doRunWithFailure();

    private void finished() {
    }

    protected List<String> getAllArguments() {
        List<String> allArguments = new ArrayList<>();

        // JVM arguments
        allArguments.addAll(getImplicitJvmSystemProperties().entrySet().stream().map(entry -> String.format("-D%s=%s", entry.getKey(), entry.getValue())).collect(toList()));

        // Gradle arguments
        allArguments.addAll(configuration.getBuildScript().getAsArguments());
        allArguments.addAll(configuration.getProjectDirectory().getAsArguments());
        allArguments.addAll(configuration.getInitScripts().getAsArguments());
        allArguments.addAll(configuration.getSettingsFile().getAsArguments());

        if (noDaemonArgumentGiven()) {
            allArguments.addAll(getDaemonArguments());
        }

        allArguments.addAll(configuration.getShowStacktrace().getAsArguments());

        // Deal with missing settings.gradle[.kts] file
        configuration.getMissingSettingsFilePolicy().ensureAvailable(() -> getTestDirectory(), WorkingDirectory.of(getWorkingDirectory()));

        // This will cause problems on Windows if the path to the Gradle executable that is used has a space in it (e.g. the user's dir is c:/Users/John Smith/)
        // This is fundamentally a windows issue: You can't have arguments with spaces in them if the path to the batch script has a space
        // We could work around this by setting -Dgradle.user.home but GRADLE-1730 (which affects 1.0-milestone-3) means that that
        // is problematic as well. For now, we just don't support running the int tests from a path with a space in it on Windows.
        // When we stop testing against M3 we should change to use the system property.
        allArguments.addAll(configuration.getGradleUserHomeDirectory().getAsArguments());

        allArguments.addAll(configuration.getConsoleType().getAsArguments());

        allArguments.addAll(configuration.getDeprecationChecks().getAsArguments());

        allArguments.addAll(configuration.getArguments());
        allArguments.addAll(configuration.getTasks());

        return allArguments;
    }

    protected List<String> getDaemonArguments() {
        if (usesDaemon()) {
            return ImmutableList.of("--daemon");
        }
        return ImmutableList.of("--no-daemon");
    }

    /**
     * Returns the set of system properties that should be set on every JVM used by this executer.
     */
    protected Map<String, String> getImplicitJvmSystemProperties() {
        Map<String, String> properties = new LinkedHashMap<>();

        properties.putAll(configuration.getUserHomeDirectory().getAsJvmSystemProperties());

        properties.putAll(configuration.getDaemonIdleTimeout().getAsJvmSystemProperties());
        properties.putAll(configuration.getDaemonBaseDirectory().getAsJvmSystemProperties());

        if (!configuration.getTemporaryDirectory().hasWhitespace() || (getDistribution().isSupportsSpacesInGradleAndJavaOpts() && supportsWhiteSpaceInEnvVars())) {
            properties.putAll(configuration.getTemporaryDirectory().getAsJvmSystemProperties());
        }

        properties.putAll(configuration.getDefaultCharacterEncoding().getAsJvmSystemProperties());
        properties.putAll(configuration.getDefaultLocale().getAsJvmSystemProperties());

        properties.putAll(configuration.getRenderWelcomeMessage().getAsJvmSystemProperties());
        configuration.getRenderWelcomeMessage().apply(configuration.getGradleUserHomeDirectory().get(), configuration.getDistribution().getVersion());

        return properties;
    }

    protected boolean supportsWhiteSpaceInEnvVars() {
        return true;
    }
}
