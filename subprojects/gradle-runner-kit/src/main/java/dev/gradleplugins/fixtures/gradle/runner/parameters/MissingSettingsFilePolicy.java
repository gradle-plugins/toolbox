package dev.gradleplugins.fixtures.gradle.runner.parameters;

import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;

import java.io.File;
import java.util.function.Consumer;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.createFile;
import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public enum MissingSettingsFilePolicy implements GradleExecutionParameter<MissingSettingsFilePolicy>, BeforeExecute {
    IGNORES_WHEN_MISSING(MissingSettingsFilePolicy::ignore),
    CREATE_WHEN_MISSING(MissingSettingsFilePolicy::ensureAvailable);

    private final Consumer<GradleExecutionContext> action;
    MissingSettingsFilePolicy(Consumer<GradleExecutionContext> action) {
        this.action = action;
    }

    @Override
    public void accept(GradleExecutionContext parameters) {
        action.accept(parameters);
    }

//    enum SettingsFilePolicy implements Consumer<GradleExecutionParameters> {

//        private final Consumer<GradleExecutionParameters> action;
//
//        SettingsFilePolicy(Consumer<GradleExecutionParameters> action) {
//            this.action = action;
//        }
//
//        @Override
//        public void accept(GradleExecutionParameters parameters) {
//            action.accept(parameters);
//        }
//    }
    private static void ignore(GradleExecutionContext parameters) {
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
