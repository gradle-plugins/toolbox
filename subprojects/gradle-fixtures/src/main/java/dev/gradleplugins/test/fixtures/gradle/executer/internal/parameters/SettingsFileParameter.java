package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public final class SettingsFileParameter extends GradleExecutionParameterImpl<RegularFile> implements CommandLineGradleExecutionParameter<RegularFile>, GradleExecutionParameter<RegularFile> {

    public static SettingsFileParameter unset() {
        return noValue(SettingsFileParameter.class);
    }

    public static SettingsFileParameter of(File settingsFile) {
        return fixed(SettingsFileParameter.class, new RegularFile() {
            @Override
            public File getAsFile() {
                return settingsFile;
            }
        });
    }

    public void ensureAvailable(Directory testDirectory, WorkingDirectory workingDirectory) {
        Directory directory = workingDirectory;
        while (directory != null && testDirectory.isSelfOrDescendent(directory)) {
            if (hasSettingsFile(directory)) {
                return;
            }
            directory = directory.getParentDirectory();
        }
        workingDirectory.file("settings.gradle").createFile();
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Arrays.asList("--settings-file", get().getAbsolutePath());
        }
        return Collections.emptyList();
    }

    private boolean hasSettingsFile(Directory directory) {
        if (directory.getAsFile().isDirectory()) {
            return directory.file("settings.gradle").exists() || directory.file("settings.gradle.kts").exists();
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
