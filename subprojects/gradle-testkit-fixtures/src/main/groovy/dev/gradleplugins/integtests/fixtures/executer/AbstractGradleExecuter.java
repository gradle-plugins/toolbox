package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;

public abstract class AbstractGradleExecuter implements GradleExecuter {
    private final TestDirectoryProvider testDirectoryProvider;

    public AbstractGradleExecuter(TestDirectoryProvider testDirectoryProvider) {
        this.testDirectoryProvider = testDirectoryProvider;
    }

    @Override
    public TestDirectoryProvider getTestDirectoryProvider() {
        return testDirectoryProvider;
    }
}
