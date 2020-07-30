package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
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
public class GradleExecuterConfiguration {
    private final GradleDistribution distribution;
    private final GradleExecuterBuildContext buildContext;
    @With private File workingDirectory = null;
    @With private File userHomeDirectory = null;
    @With private File gradleUserHomeDirectory = null;
    @With private boolean showStacktrace = true;
    @With private File settingsFile = null;
    @With private File buildScript = null;
    @With @NonNull private List<File> initScripts = emptyList();
    @With private File projectDirectory = null;
    @With private boolean useBuildCache = false;
    @With @NonNull private List<String> arguments = emptyList();
    @With @NonNull private List<String> tasks = emptyList();
    @With @NonNull private Map<String, ?> environment = emptyMap();
    @With private ConsoleOutput consoleType = null;
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
