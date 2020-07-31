package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;

import java.io.File;

@Value
public class TemporaryDirectory implements DirectoryParameter {
    File directory;

    @Override
    public File getAsFile() {
        return directory;
    }
}
