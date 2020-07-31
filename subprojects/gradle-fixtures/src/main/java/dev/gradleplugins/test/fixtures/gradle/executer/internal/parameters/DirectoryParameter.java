package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;

public interface DirectoryParameter {
    File getAsFile();

    default File file(Object... path) {
        return SettingsFileParameter.UnsetSettingsFileParameter.file(getAsFile(), path);
    }

    default boolean mkdirs() {
        return getAsFile().mkdirs();
    }

    default String getAbsolutePath() {
        return getAsFile().getAbsolutePath();
    }
}
