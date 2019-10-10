package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGradleExecuter implements GradleExecuter {
    private final TestDirectoryProvider testDirectoryProvider;

    public AbstractGradleExecuter(TestDirectoryProvider testDirectoryProvider) {
        this.testDirectoryProvider = testDirectoryProvider;
    }

    @Override
    public TestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }

    //region Working directory
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

    //region User home directory (java.home)
    private File userHomeDirectory = null;
    @Override
    public GradleExecuter withUserHomeDirectory(File userHomeDirectory) {
        this.userHomeDirectory = userHomeDirectory;
        return this;
    }
    //endregion

    //region Stacktrace (--stack-trace)
    private boolean showStacktrace = true;
    @Override
    public GradleExecuter withStacktraceDisabled() {
        showStacktrace = false;
        return this;
    }
    //endregion

    protected void reset() {
        workingDirectory = null;
        userHomeDirectory = null;
        showStacktrace = true;
    }

    protected List<String> getAllArguments() {
        List<String> allArguments = new ArrayList<>();

        // JVM arguments
        if (userHomeDirectory != null) {
            allArguments.add("-Duser.home=" + userHomeDirectory.getAbsolutePath());
        }

        // Gradle arguments
        if (showStacktrace) {
            allArguments.add("--stacktrace");
        }

        return allArguments;
    }
}
