package dev.gradleplugins.fixtures.gradle.runner;

import com.google.common.collect.ImmutableList;
import dev.gradleplugins.fixtures.gradle.runner.parameters.*;
import lombok.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.fixtures.gradle.runner.GradleExecutorGradleTestKitImpl.DEFAULT_TEST_KIT_DAEMON_IDLE_TIMEOUT;
import static dev.gradleplugins.fixtures.gradle.runner.GradleExecutorGradleTestKitImpl.TEST_KIT_DAEMON_DIR_NAME;

@Data
@AllArgsConstructor
final class GradleRunnerParameters implements GradleExecutionContext {
    private final GradleExecutorType executorType;
//    private final Supplier<GradleExecutionParameters> contextSupplier;
    @With @NonNull private StandardOutput standardOutput = StandardOutput.forwardToStandardOutput();
    @With @NonNull private StandardOutput standardError = StandardOutput.forwardToStandardError();
//    private final GradleExecuterBuildContext buildContext;
    @With @NonNull private GradleDistribution distribution = GradleDistribution.executorDefault();
    @With @NonNull private WorkingDirectory workingDirectory = WorkingDirectory.unset();
    @With @NonNull private MissingSettingsFilePolicy missingSettingsFilePolicy = MissingSettingsFilePolicy.CREATE_WHEN_MISSING;
    @With @NonNull private EnvironmentVariables environmentVariables = EnvironmentVariables.unset();
    @With @NonNull private JavaHome javaHome = JavaHome.current();
    @With @NonNull private ConsoleType consoleType = ConsoleType.DEFAULT;
//    @With private boolean debuggerAttached = false;
//    @With private boolean pluginClasspath = false;
//    @With private String gradleVersion = null;
//    @With @NonNull private List<Function<? super GradleExecuter, GradleExecuter>> beforeExecute = emptyList();
//    @With @NonNull private List<Consumer<? super GradleExecuter>> afterExecute = emptyList();

    // Command-line arguments
    @With @NonNull private GradleUserHomeDirectory gradleUserHomeDirectory = GradleUserHomeDirectory.testKitDirectory();
    @With @NonNull private Stacktrace stacktrace = Stacktrace.SHOW;
    @With @NonNull private BuildCache buildCache = BuildCache.DISABLED;
    @With @NonNull private CommandLineArguments arguments = CommandLineArguments.empty();
    @With @NonNull private SettingsFile settingsFile = SettingsFile.unset();
    @With @NonNull private BuildScript buildScript = BuildScript.unset();
    @With @NonNull private ProjectDirectory projectDirectory = ProjectDirectory.unset();
    @With @NonNull private InitScripts initScripts = InitScripts.empty();
    @With @NonNull private GradleTasks tasks = GradleTasks.empty();
    @With @NonNull private DeprecationChecks deprecationChecks = DeprecationChecks.FAILS;
//    @With @NonNull private TemporaryDirectoryParameter temporaryDirectory = TemporaryDirectoryParameter.implicit();

    // JVM arguments
    @With @NonNull private UserHomeDirectory userHomeDirectory = UserHomeDirectory.unset();
    @With @NonNull private CharacterEncoding defaultCharacterEncoding = CharacterEncoding.unset();
    @With @NonNull private Locale defaultLocale = Locale.defaultLocale();
    @With @NonNull private WelcomeMessage welcomeMessageRendering = WelcomeMessage.DISABLED;
    @With @NonNull private DaemonBaseDirectory daemonBaseDirectory = DaemonBaseDirectory.of(GradleUserHomeDirectory.relativeToGradleUserHome(TEST_KIT_DAEMON_DIR_NAME));
    @With @NonNull private DaemonIdleTimeout daemonIdleTimeout = DaemonIdleTimeout.of(DEFAULT_TEST_KIT_DAEMON_IDLE_TIMEOUT);


    @With @NonNull private BuildScan buildScan = BuildScan.DISABLED;
//    @With private boolean requireDaemon = false;
//    @With private boolean daemonCrashChecks = true;
//    @With private List<File> isolatedDaemonBaseDirectories = emptyList();
//    @With private List<ExecutionResult> executionResults = emptyList();

    GradleRunnerParameters(GradleExecutorType executorType) {
        this.executorType = executorType;
    }

    @Override
    public List<GradleExecutionParameter<?>> getExecutionParameters() {
        return allExecutionParameters(this);
    }

    @SneakyThrows
    static List<GradleExecutionParameter<?>> allExecutionParameters(GradleExecutionContext parameters) {
        val result = ImmutableList.<GradleExecutionParameter<?>>builder();
        for (Method method : GradleExecutionContext.class.getMethods()) {
            if (GradleExecutionParameter.class.isAssignableFrom(method.getReturnType())) {
                result.add((GradleExecutionParameter<?>) method.invoke(parameters));
            }
        }
        return result.build();
    }

    @Override
    public List<String> getAllArguments() {
        return getExecutionParameters().stream().filter(GradleExecutionCommandLineParameter.class::isInstance).flatMap(GradleRunnerParameters::asArguments).collect(Collectors.toList());
    }

    private static Stream<String> asArguments(GradleExecutionParameter<?> parameter) {
        return ((GradleExecutionCommandLineParameter<?>) parameter).getAsArguments().stream();
    }

    GradleRunnerParameters calculateValues() {
        allExecutionParameters(this).stream().filter(GradleExecutionParameterInternal.class::isInstance).forEach(it -> ((GradleExecutionParameterInternal)it).calculateValue(this));
        return this;
    }
}
