package dev.gradleplugins.fixtures.gradle.runner;

import dev.gradleplugins.fixtures.file.FileSystemFixture;

public interface GradleRunnerFixture {
    GradleRunner newRunner();

    default GradleBuildResult succeeds(String... arguments) {
        GradleRunner runner = newRunner();
        if (this instanceof FileSystemFixture) {
            runner = runner.inDirectory(((FileSystemFixture) this).getTestDirectory());
        }
        return runner.withArguments(arguments).build();
    }

    default GradleBuildResult fails(String... arguments) {
        GradleRunner runner = newRunner();
        if (this instanceof FileSystemFixture) {
            runner = runner.inDirectory(((FileSystemFixture) this).getTestDirectory());
        }
        return runner.withArguments(arguments).buildAndFail();
    }
}
