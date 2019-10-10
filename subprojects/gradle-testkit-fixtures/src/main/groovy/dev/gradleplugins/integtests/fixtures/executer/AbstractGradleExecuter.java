package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;

import java.io.File;

public abstract class AbstractGradleExecuter implements GradleExecuter {
    private final TestDirectoryProvider testDirectoryProvider;
    private File workingDirectory = null;

    public AbstractGradleExecuter(TestDirectoryProvider testDirectoryProvider) {
        this.testDirectoryProvider = testDirectoryProvider;
    }

    @Override
    public TestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }

    //region Working directory
    public File getWorkingDirectory() {
        return workingDirectory == null ? getTestDirectoryProvider().getTestDirectory() : workingDirectory;
    }

    @Override
    public GradleExecuter inDirectory(File directory) {
        workingDirectory = directory;
        return this;
    }
    //endregion

    protected void reset() {
        workingDirectory = null;
    }
}
