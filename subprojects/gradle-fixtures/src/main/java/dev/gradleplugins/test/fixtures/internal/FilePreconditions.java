package dev.gradleplugins.test.fixtures.internal;

import com.google.common.base.Preconditions;

import java.io.File;

public class FilePreconditions {
    public static <T extends File> void checkIsDirectory(T file, String message) {
        Preconditions.checkArgument(file != null, "%s, was null.", message);
        Preconditions.checkArgument(file.exists(), "%s, path '%s' does not exists.", message, file.getAbsolutePath());
        Preconditions.checkArgument(!file.isFile(), "%s, path '%s' is a file.", message, file.getAbsolutePath());
        Preconditions.checkArgument(file.isDirectory(), "%s, path '%s' is not a directory.", message, file.getAbsolutePath());
    }
}
