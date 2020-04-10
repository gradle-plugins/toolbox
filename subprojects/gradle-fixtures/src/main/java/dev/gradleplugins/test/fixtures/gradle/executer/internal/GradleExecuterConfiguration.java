package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import dev.gradleplugins.test.fixtures.logging.ConsoleOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
public class GradleExecuterConfiguration {
    @With private File workingDirectory = null;
    @With private File userHomeDirectory = null;
    @With private File gradleUserHomeDirectory = null;
    @With private boolean showStacktrace = true;
    @With private File settingsFile = null;
    @With private File buildScript = null;
    @With @NonNull private List<File> initScripts = new ArrayList<>();
    @With private File projectDirectory = null;
    @With private boolean useBuildCache = false;
    @With @NonNull private List<String> arguments = new ArrayList<>();
    @With @NonNull private List<String> tasks = new ArrayList<>();
    @With @NonNull private Map<String, ?> environment = new HashMap<>();
    @With private ConsoleOutput consoleType = null;
    @With private boolean debuggerAttached = false;
    @With private boolean pluginClasspath = false;
    @With private String gradleVersion = null;
    @With @NonNull private List<Consumer<? super GradleExecuter>> beforeExecute = new ArrayList<>();
    @With @NonNull private List<Consumer<? super GradleExecuter>> afterExecute = new ArrayList<>();

    public GradleExecuterConfiguration() {
        this(null, null, null, true, null, null, Collections.emptyList(), null, false, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), null, false, false, null, Collections.emptyList(), Collections.emptyList());
    }
}
