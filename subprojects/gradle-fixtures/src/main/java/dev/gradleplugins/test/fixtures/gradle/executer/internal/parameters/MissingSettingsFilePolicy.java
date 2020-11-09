package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public abstract class MissingSettingsFilePolicy {
    public abstract void ensureAvailable(Directory testDirectory, WorkingDirectory workingDirectory);

    public static MissingSettingsFilePolicy ignores() {
        return new IgnoresWhenMissingPolicy();
    }

    public static MissingSettingsFilePolicy creates() {
        return new CreateWhenMissingPolicy();
    }

    private static final class IgnoresWhenMissingPolicy extends MissingSettingsFilePolicy {
        @Override
        public void ensureAvailable(Directory testDirectory, WorkingDirectory workingDirectory) {

        }
    }

    private static final class CreateWhenMissingPolicy extends MissingSettingsFilePolicy {
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

        private static boolean hasSettingsFile(Directory directory) {
            if (directory.getAsFile().isDirectory()) {
                return directory.file("settings.gradle").exists() || directory.file("settings.gradle.kts").exists();
            }
            return false;
        }
    }

}
