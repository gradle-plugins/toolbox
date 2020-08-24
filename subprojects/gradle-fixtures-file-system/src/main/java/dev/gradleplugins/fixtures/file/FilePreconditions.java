package dev.gradleplugins.fixtures.file;

import com.google.common.base.Preconditions;

import java.io.File;

public class FilePreconditions {
    public static File checkIsDirectory(File file, String message) {
        Preconditions.checkArgument(file != null, "%s because the specified directory is null.", message);
        Preconditions.checkArgument(file.exists(), "%s because '%s' does not exists.", message, file.getAbsolutePath());
        Preconditions.checkArgument(!file.isFile(), "%s because '%s' is a file.", message, file.getAbsolutePath());
        Preconditions.checkArgument(file.isDirectory(), "%s because '%s' is not a directory.", message, file.getAbsolutePath());
        return file;
    }

    public static File checkNotExistingFile(File file, String message) {
        Preconditions.checkArgument(file != null, "%s because the specified file is null.", message);
        Preconditions.checkArgument(!file.exists() || !file.isDirectory(), "%s because '%s' is a directory.", message, file.getAbsolutePath());
        return file;
    }

    public static File checkNotExistingDirectory(File file, String message) {
        Preconditions.checkArgument(file != null, "%s because the specified directory is null.", message);
        Preconditions.checkArgument(!file.exists() || !file.isFile(), "%s because '%s' is a file.", message, file.getAbsolutePath());
        return file;
    }

    public static File checkIsNotNull(File file, String message) {
        Preconditions.checkArgument(file != null, "%s because the specified file is null.", message);
        return file;
    }
}
