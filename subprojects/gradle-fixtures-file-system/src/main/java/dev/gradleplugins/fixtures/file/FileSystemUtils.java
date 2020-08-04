package dev.gradleplugins.fixtures.file;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class FileSystemUtils {
    public static File createFile(File self) {
        createDirectory(self.getParentFile());
        try {
            assertThat(self.isFile() || self.createNewFile(), equalTo(true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return self;
    }

    public static File createDirectory(File self) {
        if (self.mkdirs()) {
            return self;
        }
        if (self.isDirectory()) {
            return self;
        }
        throw new AssertionError("Problems creating dir: " + self
                + ". Diagnostics: exists=" + self.exists() + ", isFile=" + self.isFile() + ", isDirectory=" + self.isDirectory());
    }

    public static File file(File self, Object... path) {
        return join(self, path);
    }

    private static File join(File file, Object[] path) {
        File current = file.getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }
        try {
            return current.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not canonicalise '%s'.", current), e);
        }
    }

    public static boolean isSelfOrDescendent(File self, File file) {
        if (file.getAbsolutePath().equals(self.getAbsolutePath())) {
            return true;
        }
        return file.getAbsolutePath().startsWith(self.getAbsolutePath() + File.separatorChar);
    }
}
