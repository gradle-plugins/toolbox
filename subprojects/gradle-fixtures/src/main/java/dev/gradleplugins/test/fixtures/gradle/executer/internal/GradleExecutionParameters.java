package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters.*;
import lombok.*;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.emptyList;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Deprecated
public class GradleExecutionParameters {
    private final GradleDistribution distribution;
    private final GradleExecuterBuildContext buildContext;
    @With @NonNull private WorkingDirectoryParameter workingDirectory = WorkingDirectoryParameter.unset();
    @With @NonNull private UserHomeDirectoryParameter userHomeDirectory = UserHomeDirectoryParameter.unset();
    @With @NonNull private GradleUserHomeDirectoryParameter gradleUserHomeDirectory = GradleUserHomeDirectoryParameter.unset();
    @With @NonNull private StacktraceParameter showStacktrace = StacktraceParameter.show();
    @With @NonNull private SettingsFileParameter settingsFile = SettingsFileParameter.unset();
    @With @NonNull private BuildScriptParameter buildScript = BuildScriptParameter.unset();
    @With @NonNull private InitScriptsParameter initScripts = InitScriptsParameter.empty();
    @With @NonNull private ProjectDirectoryParameter projectDirectory = ProjectDirectoryParameter.unset();
    @With @NonNull private List<String> arguments = emptyList();
    @With @NonNull private List<String> tasks = emptyList();
    @With @NonNull private EnvironmentVariablesParameter environment = EnvironmentVariablesParameter.empty();
    @With @NonNull private ConsoleTypeParameter consoleType = ConsoleTypeParameter.unset();
    @With private boolean debuggerAttached = false;
    @With private boolean pluginClasspath = false;
    @With private String gradleVersion = null;
    @With @NonNull private List<Function<? super GradleExecuter, GradleExecuter>> beforeExecute = emptyList();
    @With @NonNull private List<Consumer<? super GradleExecuter>> afterExecute = emptyList();
    @With @NonNull private DeprecationChecksParameter deprecationChecks = DeprecationChecksParameter.fails();
    @With @NonNull private DaemonBaseDirectoryParameter daemonBaseDirectory = DaemonBaseDirectoryParameter.unset();
    @With @NonNull private DaemonIdleTimeoutParameter daemonIdleTimeout = DaemonIdleTimeoutParameter.of(Duration.ofSeconds(120));
    @With @NonNull private TemporaryDirectoryParameter temporaryDirectory = TemporaryDirectoryParameter.implicit();
    @With @NonNull private CharacterEncodingParameter defaultCharacterEncoding = CharacterEncodingParameter.defaultCharset();
    @With @NonNull private LocaleParameter defaultLocale = LocaleParameter.unset();
    @With @NonNull private WelcomeMessageParameter renderWelcomeMessage = WelcomeMessageParameter.disabled();
    @With private boolean requireDaemon = false;
    @With private boolean daemonCrashChecks = true;
    @With private List<File> isolatedDaemonBaseDirectories = emptyList();
    @With private List<ExecutionResult> executionResults = emptyList();
    @With @NonNull private MissingSettingsFilePolicy missingSettingsFilePolicy = MissingSettingsFilePolicy.creates();
}
