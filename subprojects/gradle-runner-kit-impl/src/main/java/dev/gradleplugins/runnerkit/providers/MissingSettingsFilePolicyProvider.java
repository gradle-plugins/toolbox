package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;

import java.io.File;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.createFile;
import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public final class MissingSettingsFilePolicyProvider extends AbstractGradleExecutionProvider<GradleExecutionContext.MissingSettingsFilePolicy> implements BeforeExecute {
    public static MissingSettingsFilePolicyProvider createWhenMissing() {
        return fixed(MissingSettingsFilePolicyProvider.class, GradleExecutionContext.MissingSettingsFilePolicy.CREATE_WHEN_MISSING);
    }

    public static MissingSettingsFilePolicyProvider ignoresWhenMissing() {
        return fixed(MissingSettingsFilePolicyProvider.class, GradleExecutionContext.MissingSettingsFilePolicy.IGNORES_WHEN_MISSING);
    }

    public void validate() {
        switch (get()) {
            case CREATE_WHEN_MISSING:
            case IGNORES_WHEN_MISSING:
                return;
        }
        throw new IllegalArgumentException("Unaccounted for values");
    }

    @Override
    public void accept(GradleExecutionContext parameters) {
        if (get().equals(GradleExecutionContext.MissingSettingsFilePolicy.CREATE_WHEN_MISSING)) {
            ensureAvailable(parameters);
        }
    }

    private static void ensureAvailable(GradleExecutionContext parameters) {
        if (parameters.getWorkingDirectory().isPresent()) {
            File directory = parameters.getWorkingDirectory().get();
//        while (directory != null && FileSystemUtils.isSelfOrDescendent(testDirectory.isSelfOrDescendent(directory)) {
            if (hasSettingsFile(directory)) {
                return;
            }
//            directory = directory.getParentFile();
//        }
            createFile(file(directory, "settings.gradle"));
        }
    }

    private static boolean hasSettingsFile(File directory) {
        if (directory.isDirectory()) {
            return file(directory, "settings.gradle").exists() || file(directory, "settings.gradle.kts").exists();
        }
        return false;
    }

//    @EqualsAndHashCode(callSuper = false)
//    private static final class IgnoresWhenMissingPolicy extends MissingSettingsFilePolicy {
////        @Override
////        public void ensureAvailable(Directory testDirectory, WorkingDirectory workingDirectory) {
////
////        }
//    }
//
//    @EqualsAndHashCode(callSuper = false)
//    private static final class CreateWhenMissingPolicy extends MissingSettingsFilePolicy {
////        public void ensureAvailable(Directory testDirectory, WorkingDirectory workingDirectory) {
////            Directory directory = workingDirectory;
////            while (directory != null && testDirectory.isSelfOrDescendent(directory)) {
////                if (hasSettingsFile(directory)) {
////                    return;
////                }
////                directory = directory.getParentDirectory();
////            }
////            workingDirectory.file("settings.gradle").createFile();
////        }
////
////        private static boolean hasSettingsFile(Directory directory) {
////            if (directory.getAsFile().isDirectory()) {
////                return directory.file("settings.gradle").exists() || directory.file("settings.gradle.kts").exists();
////            }
////            return false;
////        }
//    }

}
