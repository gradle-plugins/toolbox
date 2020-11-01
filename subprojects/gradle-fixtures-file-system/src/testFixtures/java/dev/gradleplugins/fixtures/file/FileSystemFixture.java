package dev.gradleplugins.fixtures.file;

import java.io.File;

public interface FileSystemFixture {
    default File file(Object... paths) {
        return FileSystemUtils.file(getTestDirectory(), paths);
    }

    File getTestDirectory();
}
