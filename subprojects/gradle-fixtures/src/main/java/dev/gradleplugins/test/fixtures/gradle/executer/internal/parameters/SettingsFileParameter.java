package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import dev.gradleplugins.test.fixtures.file.TestFile;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public interface SettingsFileParameter extends CommandLineGradleParameter, RegularFileParameter {
    List<String> getAsArguments();

    default void ensureAvailable(File testDirectory, File workingDirectory) {}

    static SettingsFileParameter unset() {
        return new UnsetSettingsFileParameter();
    }

    static SettingsFileParameter of(File settingsFile) {
        return new DefaultSettingsFileParameter(settingsFile);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetSettingsFileParameter extends UnsetParameter<File> implements SettingsFileParameter {
        @Override
        public void ensureAvailable(File testDirectory, File workingDirectory) {
            File directory = workingDirectory;
            while (directory != null && isSelfOrDescendent(testDirectory, directory)) {
                if (hasSettingsFile(directory)) {
                    return;
                }
                directory = directory.getParentFile();
            }
            createFile(file(workingDirectory, "settings.gradle"));
        }

        private boolean hasSettingsFile(File directory) {
            if (directory.isDirectory()) {
                return file(directory, "settings.gradle").isFile() || file(directory, "settings.gradle.kts").isFile();
            }
            return false;
        }

        public static File createFile(File self) {
            createDirectory(self.getParentFile());
            try {
                assertTrue(self.isFile() || self.createNewFile());
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

        public static File file(File file, Object... path) {
            return join(file, path);
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

    @Value
    class DefaultSettingsFileParameter implements SettingsFileParameter {
        File value;

        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--settings-file", value.getAbsolutePath());
        }
    }
}
