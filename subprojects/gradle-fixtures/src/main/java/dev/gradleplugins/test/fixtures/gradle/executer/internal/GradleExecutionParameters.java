package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters.*;
import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput;
import lombok.*;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class GradleExecutionParameters {
    private final GradleDistribution distribution;
    private final GradleExecuterBuildContext buildContext;
    @With @NonNull private WorkingDirectoryParameter workingDirectory = WorkingDirectoryParameter.unset();
    @With private File userHomeDirectory = null;
    @With @NonNull private GradleUserHomeDirectoryParameter gradleUserHomeDirectory = GradleUserHomeDirectoryParameter.unset();
    @With @NonNull private StacktraceParameter showStacktrace = StacktraceParameter.show();
    @With @NonNull private SettingsFileParameter settingsFile = SettingsFileParameter.unset();
    @With @NonNull private BuildScriptParameter buildScript = BuildScriptParameter.unset();
    @With @NonNull private InitScriptsParameter initScripts = InitScriptsParameter.empty();
    @With @NonNull private ProjectDirectoryParameter projectDirectory = ProjectDirectoryParameter.unset();
    @With private boolean useBuildCache = false;
    @With @NonNull private List<String> arguments = emptyList();
    @With @NonNull private List<String> tasks = emptyList();
    @With @NonNull private EnvironmentVariablesParameter environment = EnvironmentVariablesParameter.empty();
    @With @NonNull private ConsoleTypeParameter consoleType = ConsoleTypeParameter.unset();
    @With private boolean debuggerAttached = false;
    @With private boolean pluginClasspath = false;
    @With private String gradleVersion = null;
    @With @NonNull private List<Function<? super GradleExecuter, GradleExecuter>> beforeExecute = emptyList();
    @With @NonNull private List<Consumer<? super GradleExecuter>> afterExecute = emptyList();
    @With private boolean allowDeprecations = false;
    @With private File daemonBaseDirectory = null;
    @With private Duration daemonIdleTimeout = Duration.ofSeconds(120);
    @With private boolean explicitTemporaryDirectory = true;
    @With private Charset defaultCharacterEncoding = Charset.defaultCharset();
    @With private Locale defaultLocale = null;
    @With private boolean renderWelcomeMessage = false;
    @With private boolean requireDaemon = false;
    @With private boolean daemonCrashChecks = true;
    @With private List<File> isolatedDaemonBaseDirectories = emptyList();
    @With private List<ExecutionResult> executionResults = emptyList();
}
