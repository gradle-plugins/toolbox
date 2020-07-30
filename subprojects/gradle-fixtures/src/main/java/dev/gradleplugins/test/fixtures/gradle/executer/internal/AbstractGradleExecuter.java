package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.daemon.DaemonLogsAnalyzer;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput;
import lombok.NonNull;
import lombok.val;
import org.gradle.launcher.cli.DefaultCommandLineActionFactory;
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public abstract class AbstractGradleExecuter implements GradleExecuter {
    protected GradleExecuterConfiguration configuration;
    private final TestFile testDirectory;

    public AbstractGradleExecuter(@NonNull GradleDistribution distribution, @NonNull TestFile testDirectory, @NonNull GradleExecuterBuildContext buildContext) {
        this(testDirectory, new GradleExecuterConfiguration(distribution, buildContext).withGradleUserHomeDirectory(buildContext.getGradleUserHomeDirectory()).withDaemonBaseDirectory(buildContext.getDaemonBaseDirectory()));
    }

    protected AbstractGradleExecuter(TestFile testDirectory, GradleExecuterConfiguration configuration) {
        this.configuration = configuration;
        this.testDirectory = testDirectory;
    }

    protected GradleExecuter newInstance(GradleExecuterConfiguration configuration) {
        return newInstance(testDirectory, configuration);
    }

    protected abstract GradleExecuter newInstance(TestFile testDirectory, GradleExecuterConfiguration configuration);

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
        return ofNullable(configuration.getWorkingDirectory()).orElse(testDirectory);
    }

    @Override
    public GradleExecuter inDirectory(File directory) {
        return newInstance(configuration.withWorkingDirectory(directory));
    }
    //endregion

    //region Flag `-Djava.home` configuration
    @Override
    public GradleExecuter withUserHomeDirectory(File userHomeDirectory) {
        return newInstance(configuration.withUserHomeDirectory(userHomeDirectory));
    }
    //endregion

    //region Flag `--gradle-user-home` configuration
    @Override
    public GradleExecuter withGradleUserHomeDirectory(File gradleUserHomeDirectory) {
        return newInstance(configuration.withGradleUserHomeDirectory(gradleUserHomeDirectory));
    }

    @Override
    public GradleExecuter requireOwnGradleUserHomeDirectory() {
        return newInstance(configuration.withGradleUserHomeDirectory(testDirectory.createDirectory("user-home")));
    }
    //endregion

    //region Flag `--stack-trace` configuration
    @Override
    public GradleExecuter withStacktraceDisabled() {
        return newInstance(configuration.withShowStacktrace(false));
    }
    //endregion

    //region Flag `--settings-file` configuration
    @Override
    public GradleExecuter usingSettingsFile(File settingsFile) {
        return newInstance(configuration.withSettingsFile(settingsFile));
    }

    // TODO: Maybe we should remove dependency on TestFile within this implementation
    private void ensureSettingsFileAvailable() {
        TestFile workingDirectory = new TestFile(getWorkingDirectory());
        TestFile directory = workingDirectory;
        while (directory != null && getTestDirectory().isSelfOrDescendent(directory)) {
            if (hasSettingsFile(directory)) {
                return;
            }
            directory = directory.getParentFile();
        }
        workingDirectory.createFile("settings.gradle");
    }

    private boolean hasSettingsFile(TestFile directory) {
        if (directory.isDirectory()) {
            return directory.file("settings.gradle").isFile() || directory.file("settings.gradle.kts").isFile();
        }
        return false;
    }
    //endregion

    //region Flag `--build-file` configuration
    @Override
    public GradleExecuter usingBuildScript(File buildScript) {
        return newInstance(configuration.withBuildScript(buildScript));
    }
    //endregion

    //region Flag `--init-script` configuration
    @Override
    public GradleExecuter usingInitScript(File initScript) {
        return newInstance(configuration.withInitScripts(ImmutableList.<File>builder().addAll(configuration.getInitScripts()).add(initScript).build()));
    }
    //endregion

    //region Flag `--project-dir` configuration
    @Override
    public GradleExecuter usingProjectDirectory(File projectDirectory) {
        return newInstance(configuration.withProjectDirectory(projectDirectory));
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
        return newInstance(configuration.withConsoleType(consoleType));
    }
    //endregion

    //region Environment variables configuration
    @Override
    public GradleExecuter withEnvironmentVars(Map<String, ?> environment) {
        Map<String, Object> env = Maps.newHashMap(configuration.getEnvironment());
        env.putAll(environment);
        return newInstance(configuration.withEnvironment(env));
    }
    //endregion

    //region Deprecation warning checks configuration
    @Override
    public GradleExecuter withoutDeprecationChecks() {
        return newInstance(configuration.withAllowDeprecations(true));
    }
    //endregion

    //region Daemon configuration
    @Override
    public GradleExecuter requireIsolatedDaemons() {
        return withDaemonBaseDirectory(getTestDirectory().file("daemon"));
    }

    @Override
    public GradleExecuter withDaemonBaseDirectory(File daemonBaseDirectory) {
        return newInstance(configuration.withDaemonBaseDirectory(daemonBaseDirectory));
    }

    @Override
    public GradleExecuter withDaemonIdleTimeout(Duration daemonIdleTimeout) {
        return newInstance(configuration.withDaemonIdleTimeout(daemonIdleTimeout));
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
        return configuration.getDaemonBaseDirectory().equals(configuration.getBuildContext().getDaemonBaseDirectory());
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
        return newInstance(configuration.withExplicitTemporaryDirectory(false));
    }
    //endregion

    //region Default character encoding configuration
    @Override
    public GradleExecuter withDefaultCharacterEncoding(Charset defaultCharacterEncoding) {
        return newInstance(configuration.withDefaultCharacterEncoding(defaultCharacterEncoding));
    }
    //endregion

    //region Default locale configuration
    @Override
    public GradleExecuter withDefaultLocale(Locale defaultLocale) {
        return newInstance(configuration.withDefaultLocale(defaultLocale));
    }
    //endregion

    //region Welcome message configuration
    @Override
    public GradleExecuter withWelcomeMessageEnabled() {
        return newInstance(configuration.withRenderWelcomeMessage(true));
    }
    //endregion

    @Override
    public ExecutionResult run() {
        beforeBuildSetup();
        if (configuration.getBeforeExecute().isEmpty()) {
            try {
                ExecutionResult result = doRun();
                fireAfterExecute();
                return result;
            } finally {
                finished();
            }
        } else {
            return fireBeforeExecute().run();
        }
    }

    protected abstract ExecutionResult doRun();

    @Override
    public ExecutionFailure runWithFailure() {
        beforeBuildSetup();
        if (configuration.getBeforeExecute().isEmpty()) {
            try {
                ExecutionFailure result = doRunWithFailure();
                fireAfterExecute();
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
            configuration = configuration.withIsolatedDaemonBaseDirectories(ImmutableList.<File>builder().addAll(configuration.getIsolatedDaemonBaseDirectories()).add(configuration.getDaemonBaseDirectory()).build());
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
        allArguments.addAll(ofNullable(configuration.getBuildScript()).map(it -> asList("--build-file", it.getAbsolutePath())).orElse(emptyList()));
        allArguments.addAll(ofNullable(configuration.getBuildScript()).map(it -> asList("--project-dir", it.getAbsolutePath())).orElse(emptyList()));
        allArguments.addAll(ofNullable(configuration.getInitScripts()).map(it -> it.stream().flatMap(initScript -> Stream.of("--init-script", initScript.getAbsolutePath())).collect(toList())).orElse(emptyList()));
        allArguments.addAll(ofNullable(configuration.getSettingsFile()).map(it -> asList("--settings-file", it.getAbsolutePath())).orElse(emptyList()));

        if (noDaemonArgumentGiven()) {
            allArguments.addAll(getDaemonArguments());
        }

        allArguments.addAll(configuration.isShowStacktrace() ? singletonList("--stacktrace") : emptyList());

        // Deal with missing settings.gradle[.kts] file
        if (configuration.getSettingsFile() == null) {
            ensureSettingsFileAvailable();
        }

        // This will cause problems on Windows if the path to the Gradle executable that is used has a space in it (e.g. the user's dir is c:/Users/John Smith/)
        // This is fundamentally a windows issue: You can't have arguments with spaces in them if the path to the batch script has a space
        // We could work around this by setting -Dgradle.user.home but GRADLE-1730 (which affects 1.0-milestone-3) means that that
        // is problematic as well. For now, we just don't support running the int tests from a path with a space in it on Windows.
        // When we stop testing against M3 we should change to use the system property.
        allArguments.addAll(ofNullable(configuration.getGradleUserHomeDirectory()).map(it -> asList("--gradle-user-home", it.getAbsolutePath())).orElse(emptyList()));

        allArguments.addAll(ofNullable(configuration.getConsoleType()).map(it -> asList("--console", it.toString().toLowerCase())).orElse(emptyList()));

        allArguments.addAll(configuration.isAllowDeprecations() ? emptyList() : asList("--warning-mode", "fail"));

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

        properties.putAll(ofNullable(configuration.getUserHomeDirectory()).map(it -> Collections.singletonMap("user.home", it.getAbsolutePath())).orElse(emptyMap()));

        properties.put(DaemonBuildOptions.IdleTimeoutOption.GRADLE_PROPERTY, String.valueOf(configuration.getDaemonIdleTimeout().toMillis()));
        properties.put(DaemonBuildOptions.BaseDirOption.GRADLE_PROPERTY, configuration.getDaemonBaseDirectory().getAbsolutePath());

        if (configuration.isExplicitTemporaryDirectory()) {
            val temporaryDirectory = configuration.getBuildContext().getTemporaryDirectory();
            temporaryDirectory.mkdirs(); // ignore return code
            String temporaryDirectoryPath = temporaryDirectory.getAbsolutePath();
            if (!temporaryDirectoryPath.contains(" ") || (getDistribution().isSupportsSpacesInGradleAndJavaOpts() && supportsWhiteSpaceInEnvVars())) {
                properties.put("java.io.tmpdir", temporaryDirectoryPath);
            }
        }

        properties.put("file.encoding", configuration.getDefaultCharacterEncoding().name());
        properties.putAll(ofNullable(configuration.getDefaultLocale()).map(locale -> {
            Map<String, String> result = new HashMap<>();
            result.put("user.language", locale.getLanguage());
            result.put("user.country", locale.getCountry());
            result.put("user.variant", locale.getVariant());
            return result;
        }).orElse(emptyMap()));

        properties.put(DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY, Boolean.toString(configuration.isRenderWelcomeMessage()));

        return properties;
    }

    protected boolean supportsWhiteSpaceInEnvVars() {
        return true;
    }
}
