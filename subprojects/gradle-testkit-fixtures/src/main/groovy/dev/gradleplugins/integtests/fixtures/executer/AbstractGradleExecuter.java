package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;
import dev.gradleplugins.test.fixtures.file.TestFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

abstract class AbstractGradleExecuter implements GradleExecuter {
    private final TestDirectoryProvider testDirectoryProvider;

    public AbstractGradleExecuter(TestDirectoryProvider testDirectoryProvider) {
        assert testDirectoryProvider != null : "testDirectoryProvider cannot be null";
        this.testDirectoryProvider = testDirectoryProvider;
    }

    @Override
    public TestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }

    //region Working directory configuration
    private File workingDirectory = null;
    public File getWorkingDirectory() {
        return workingDirectory == null ? getTestDirectoryProvider().getTestDirectory() : workingDirectory;
    }

    @Override
    public GradleExecuter inDirectory(File directory) {
        workingDirectory = directory;
        return this;
    }
    //endregion

    //region Flag `-Djava.home` configuration
    private File userHomeDirectory = null;
    @Override
    public GradleExecuter withUserHomeDirectory(File userHomeDirectory) {
        this.userHomeDirectory = userHomeDirectory;
        return this;
    }
    //endregion

    //region Flag `--stack-trace` configuration
    private boolean showStacktrace = true;
    @Override
    public GradleExecuter withStacktraceDisabled() {
        showStacktrace = false;
        return this;
    }
    //endregion

    //region Flag `--settings-file` configuration
    private File settingsFile = null;

    @Override
    public GradleExecuter usingSettingsFile(File settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }

    // TODO: Maybe we should remove dependency on TestFile within this implementation
    private void ensureSettingsFileAvailable() {
        TestFile workingDirectory = new TestFile(getWorkingDirectory());
        TestFile directory = workingDirectory;
        while (directory != null && getTestDirectoryProvider().getTestDirectory().isSelfOrDescendent(directory)) {
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
    private File buildScript = null;

    @Override
    public GradleExecuter usingBuildScript(File buildScript) {
        this.buildScript = buildScript;
        return this;
    }
    //endregion

    //region Flag `--project-dir` configuration
    private File projectDirectory = null;

    @Override
    public GradleExecuter usingProjectDirectory(File projectDirectory) {
        this.projectDirectory = projectDirectory;
        return this;
    }
    //endregion

    //region Flag `--build-cache` configuration
    @Override
    public GradleExecuter withBuildCacheEnabled() {
        return withArgument("--build-cache");
    }
    //endregion

    //region Process arguments configuration
    private final List<String> arguments = new ArrayList<>();

    @Override
    public GradleExecuter withArguments(String... args) {
        return withArguments(Arrays.asList(args));
    }

    @Override
    public GradleExecuter withArguments(List<String> args) {
        arguments.clear();
        arguments.addAll(args);
        return this;
    }

    @Override
    public GradleExecuter withArgument(String arg) {
        arguments.add(arg);
        return this;
    }
    //endregion

    //region Gradle tasks configuration
    private final List<String> tasks = new ArrayList<>();

    @Override
    public GradleExecuter withTasks(String... tasks) {
        return withTasks(Arrays.asList(tasks));
    }

    @Override
    public GradleExecuter withTasks(List<String> tasks) {
        this.tasks.addAll(tasks);
        return this;
    }
    //endregion

    //region Before execute actions
    private final List<Consumer<? super GradleExecuter>> beforeExecute = new ArrayList<>();

    @Override
    public void beforeExecute(Consumer<? super GradleExecuter> action) {
        beforeExecute.add(action);
    }

    private void fireBeforeExecute() {
        beforeExecute.forEach(it -> it.accept(this));
    }
    //endregion

    //region After execute actions
    private final List<Consumer<? super GradleExecuter>> afterExecute = new ArrayList<>();

    @Override
    public void afterExecute(Consumer<? super GradleExecuter> action) {
        afterExecute.add(action);
    }

    private void fireAfterExecute() {
        afterExecute.forEach(it -> it.accept(this));
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
        reset();
    }

    protected void reset() {
        arguments.clear();
        tasks.clear();

        workingDirectory = null;
        userHomeDirectory = null;
        settingsFile = null;
        buildScript = null;
        projectDirectory = null;

        showStacktrace = true;
    }

    protected List<String> getAllArguments() {
        List<String> allArguments = new ArrayList<>();

        // JVM arguments
        if (userHomeDirectory != null) {
            allArguments.add("-Duser.home=" + userHomeDirectory.getAbsolutePath());
        }

        // Gradle arguments
        if (buildScript != null) {
            allArguments.add("--build-file");
            allArguments.add(buildScript.getAbsolutePath());
        }
        if (projectDirectory != null) {
            allArguments.add("--project-dir");
            allArguments.add(projectDirectory.getAbsolutePath());
        }
        if (settingsFile != null) {
            allArguments.add("--settings-file");
            allArguments.add(settingsFile.getAbsolutePath());
        }
        if (showStacktrace) {
            allArguments.add("--stacktrace");
        }

        // Deal with missing settings.gradle[.kts] file
        if (settingsFile == null) {
            ensureSettingsFileAvailable();
        }

        allArguments.addAll(arguments);
        allArguments.addAll(tasks);

        return allArguments;
    }
}
