package dev.gradleplugins.test.fixtures.gradle.executer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.GradleExecuterConfiguration;
import dev.gradleplugins.test.fixtures.logging.ConsoleOutput;
import lombok.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

abstract class AbstractGradleExecuter implements GradleExecuter {
    protected GradleExecuterConfiguration configuration;
    private final TestFile testDirectory;

    public AbstractGradleExecuter(@NonNull TestFile testDirectory) {
        this(testDirectory, new GradleExecuterConfiguration());
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
    public GradleExecuter beforeExecute(Consumer<? super GradleExecuter> action) {
        return newInstance(configuration.withBeforeExecute(ImmutableList.<Consumer<? super GradleExecuter>>builder().addAll(configuration.getBeforeExecute()).add(action).build()));
    }

    private void fireBeforeExecute() {
        configuration.getBeforeExecute().forEach(it -> it.accept(this));
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

    @Override
    public ExecutionResult run() {
        fireBeforeExecute();
        try {
            ExecutionResult result = doRun();
            fireAfterExecute();
            return result;
        } finally {
            finished();
        }
    }

    protected abstract ExecutionResult doRun();

    @Override
    public ExecutionFailure runWithFailure() {
        fireBeforeExecute();
        try {
            ExecutionFailure result = doRunWithFailure();
            fireAfterExecute();
            return result;
        } finally {
            finished();
        }
    }

    protected abstract ExecutionFailure doRunWithFailure();

    private void finished() {
    }

    protected List<String> getAllArguments() {
        List<String> allArguments = new ArrayList<>();

        // JVM arguments
        allArguments.addAll(ofNullable(configuration.getUserHomeDirectory()).map(it -> singletonList("-Duser.home=" + it.getAbsolutePath())).orElse(emptyList()));

        // Gradle arguments
        allArguments.addAll(ofNullable(configuration.getBuildScript()).map(it -> asList("--build-file", it.getAbsolutePath())).orElse(emptyList()));
        allArguments.addAll(ofNullable(configuration.getBuildScript()).map(it -> asList("--project-dir", it.getAbsolutePath())).orElse(emptyList()));
        allArguments.addAll(ofNullable(configuration.getInitScripts()).map(it -> it.stream().flatMap(initScript -> Stream.of("--init-script", initScript.getAbsolutePath())).collect(toList())).orElse(emptyList()));
        allArguments.addAll(ofNullable(configuration.getSettingsFile()).map(it -> asList("--settings-file", it.getAbsolutePath())).orElse(emptyList()));
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

        allArguments.addAll(configuration.getArguments());
        allArguments.addAll(configuration.getTasks());

        return allArguments;
    }
}
