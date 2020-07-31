package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;

public interface Directory {
    File getAsFile();

    default RegularFile file(Object... path) {
        return new RegularFile() {
            @Override
            public File getAsFile() {
                return SettingsFileParameter.file(Directory.this.getAsFile(), path);
            }
        };
    }

    default boolean mkdirs() {
        return getAsFile().mkdirs();
    }

    default String getAbsolutePath() {
        return getAsFile().getAbsolutePath();
    }

    default boolean isSelfOrDescendent(Directory directory) {
        return SettingsFileParameter.isSelfOrDescendent(getAsFile(), directory.getAsFile());
    }

    default Directory getParentDirectory() {
        return () -> Directory.this.getAsFile().getParentFile();
    }
}
