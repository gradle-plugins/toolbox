package dev.gradleplugins.runnerkit;

import dev.gradleplugins.runnerkit.providers.*;
import lombok.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
final class GradleRunnerParameters implements GradleExecutionContext {
    private final Class<? extends GradleExecutor> executorType;

    @With @NonNull private BeforeExecuteActionsProvider beforeExecute = BeforeExecuteActionsProvider.empty();
    @With @NonNull private AfterExecuteActionsProvider afterExecute = AfterExecuteActionsProvider.empty();

    @With @NonNull private StandardStreamProvider standardOutput = StandardStreamProvider.forwardToStandardOutput();
    @With @NonNull private StandardStreamProvider standardError = StandardStreamProvider.forwardToStandardError();
    @With @NonNull private InjectedClasspathProvider injectedClasspath = InjectedClasspathProvider.empty();
//    private final GradleExecuterBuildContext buildContext;
    @With @NonNull private GradleDistributionProvider distribution = GradleDistributionProvider.executorDefault();
    @With @NonNull private WorkingDirectoryProvider workingDirectory = WorkingDirectoryProvider.unset();
    @With @NonNull private MissingSettingsFilePolicyProvider missingSettingsFilePolicy = MissingSettingsFilePolicyProvider.createWhenMissing();
    @With @NonNull private EnvironmentVariablesProvider environmentVariables = EnvironmentVariablesProvider.inherited();
    @With @NonNull private JavaHomeProvider javaHome = JavaHomeProvider.inherited();
    @With @NonNull private ConsoleTypeProvider consoleType = ConsoleTypeProvider.defaultConsole();
//    @With private boolean debuggerAttached = false;
//    @With private boolean pluginClasspath = false;
//    @With private String gradleVersion = null;
//    @With @NonNull private List<Function<? super GradleExecuter, GradleExecuter>> beforeExecute = emptyList();
//    @With @NonNull private List<Consumer<? super GradleExecuter>> afterExecute = emptyList();

    // Command-line arguments
    @With @NonNull private GradleUserHomeDirectoryProvider gradleUserHomeDirectory = GradleUserHomeDirectoryProvider.testKitDirectory();
    @With @NonNull private StacktraceProvider stacktrace = StacktraceProvider.show();
    @With @NonNull private BuildCacheProvider buildCache = BuildCacheProvider.disabled();
    @With @NonNull private CommandLineArgumentsProvider arguments = CommandLineArgumentsProvider.empty();
    @With @NonNull private SettingsFileProvider settingsFile = SettingsFileProvider.unset();
    @With @NonNull private BuildScriptProvider buildScript = BuildScriptProvider.unset();
    @With @NonNull private ProjectDirectoryProvider projectDirectory = ProjectDirectoryProvider.useWorkingDirectoryImplicitly();
    @With @NonNull private InitScriptsProvider initScripts = InitScriptsProvider.empty();
    @With @NonNull private GradleTasksProvider tasks = GradleTasksProvider.empty();
    @With @NonNull private DeprecationChecksProvider deprecationChecks = DeprecationChecksProvider.fails();
//    @With @NonNull private TemporaryDirectoryParameter temporaryDirectory = TemporaryDirectoryParameter.implicit();

    // JVM arguments
    @With @NonNull private UserHomeDirectoryProvider userHomeDirectory = UserHomeDirectoryProvider.implicit();
    @With @NonNull private CharacterEncodingProvider defaultCharacterEncoding = CharacterEncodingProvider.defaultCharset();
    @With @NonNull private LocaleProvider defaultLocale = LocaleProvider.defaultLocale();
    @With @NonNull private WelcomeMessageProvider welcomeMessageRendering = WelcomeMessageProvider.disabled();
    @With @NonNull private DaemonBaseDirectoryProvider daemonBaseDirectory = DaemonBaseDirectoryProvider.testKitDaemonDirectory();
    @With @NonNull private DaemonIdleTimeoutProvider daemonIdleTimeout = DaemonIdleTimeoutProvider.testKitIdleTimeout();


    @With @NonNull private BuildScanProvider buildScan = BuildScanProvider.disabled();
//    @With private boolean requireDaemon = false;
//    @With private boolean daemonCrashChecks = true;
//    @With private List<File> isolatedDaemonBaseDirectories = emptyList();
//    @With private List<ExecutionResult> executionResults = emptyList();

    GradleRunnerParameters(Class<? extends GradleExecutor> executorType) {
        this.executorType = executorType;
    }

    @Override
    public List<GradleExecutionProvider<?>> getExecutionParameters() {
        return allExecutionParameters(this);
    }

    @SneakyThrows
    static List<GradleExecutionProvider<?>> allExecutionParameters(GradleExecutionContext parameters) {
        val result = new ArrayList<GradleExecutionProvider<?>>();
        for (Method method : GradleExecutionContext.class.getMethods()) {
            if (GradleExecutionProvider.class.isAssignableFrom(method.getReturnType())) {
                result.add((GradleExecutionProvider<?>) method.invoke(parameters));
            }
        }
        return result;
    }

    @Override
    public List<String> getAllArguments() {
        return getExecutionParameters().stream().filter(GradleExecutionCommandLineProvider.class::isInstance).flatMap(GradleRunnerParameters::asArguments).collect(Collectors.toList());
    }

    private static Stream<String> asArguments(GradleExecutionProvider<?> parameter) {
        return ((GradleExecutionCommandLineProvider) parameter).getAsArguments().stream();
    }

    GradleRunnerParameters calculateValues() {
        allExecutionParameters(this).stream().filter(GradleExecutionProviderInternal.class::isInstance).forEach(it -> ((GradleExecutionProviderInternal<?>)it).calculateValue(this));
        return this;
    }
}
