package dev.gradleplugins.runnerkit;

import dev.gradleplugins.fixtures.file.FileSystemFixture;

public interface GradleRunnerFixture {
    GradleRunner newRunner();

    default BuildResult succeeds(String... arguments) {
        GradleRunner runner = newRunner();
        if (this instanceof FileSystemFixture) {
            runner = runner.inDirectory(((FileSystemFixture) this).getTestDirectory());
        }
        return runner.withArguments(arguments).build();
    }

    default BuildResult fails(String... arguments) {
        GradleRunner runner = newRunner();
        if (this instanceof FileSystemFixture) {
            runner = runner.inDirectory(((FileSystemFixture) this).getTestDirectory());
        }
        return runner.withArguments(arguments).buildAndFail();
    }
}
