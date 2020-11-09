package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import lombok.val;

import java.io.File;
import java.util.Optional;

/**
 * Provides values that are set during the build, or defaulted when not running in a build context (e.g. IDE).
 */
@Deprecated
public class DefaultGradleExecuterBuildContext implements GradleExecuterBuildContext {
    // Collect this early, as the process' current directory can change during embedded test execution
    public static final File TEST_DIR = new File(System.getProperty("user.dir"));
    public static final DefaultGradleExecuterBuildContext INSTANCE = new DefaultGradleExecuterBuildContext();

    @Override
    public File getGradleHomeDirectory() {
        return file("dev.gradleplugins.gradleHomeDirectory", null);
    }

    @Override
    public File getDaemonBaseDirectory() {
        return file("dev.gradleplugins.daemon.registry", "build/daemon");
    }

    @Override
    public File getGradleUserHomeDirectory() {
        return new File(file("dev.gradleplugins.gradleUserHomeDirectory", "intTestHomeDir"),"worker-1");
    }

    @Override
    public File getTemporaryDirectory() {
        return file("dev.gradleplugins.temporaryDirectory", "build/tmp");
    }

    public File getNativeServicesDir() {
        return new File(getGradleUserHomeDirectory(), "native");
    }

    protected static File file(String propertyName, String defaultPath) {
        val testFile = optionalFile(propertyName);
        if (testFile.isPresent()) {
            return testFile.get();
        }
        if (defaultPath == null) {
            throw new RuntimeException("You must set the '" + propertyName + "' property to run the Gradle tests using Gradle fixtures.");
        }
        return testFile(defaultPath);
    }

    private static Optional<File> optionalFile(String propertyName) {
        String path = System.getProperty(propertyName);
        // MODULE_WORKING_DIR doesn't seem to work correctly and MODULE_DIR seems to be in `.idea/modules/<path-to-subproject>`
        // See https://youtrack.jetbrains.com/issue/IDEA-194910
        return path != null ? Optional.of(new File(path.replace(".idea/modules/", ""))) : Optional.empty();
    }

    private static File testFile(String path) {
        File file = new File(path);
        return file.isAbsolute()
            ? file
            : new File(TEST_DIR, path).getAbsoluteFile();
    }

}