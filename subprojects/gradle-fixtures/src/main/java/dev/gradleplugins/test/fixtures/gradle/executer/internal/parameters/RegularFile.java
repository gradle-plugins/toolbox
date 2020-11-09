package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Deprecated
public interface RegularFile {
    File getAsFile();

    default boolean delete() {
        return getAsFile().delete();
    }

    default void touch() throws IOException {
        FileUtils.touch(getAsFile());
    }

    default RegularFile createFile() {
        val result = FileSystemUtils.createFile(getAsFile());
        return () -> result;
    }

    default String getAbsolutePath() {
        return getAsFile().getAbsolutePath();
    }

    default boolean exists() {
        // TODO: Throw exception if exists but not file
        return getAsFile().exists() && getAsFile().isFile();
    }
}
