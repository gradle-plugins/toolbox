package dev.gradleplugins.test.fixtures.gradle.executer;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.CurrentGradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.ReleasedGradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistribution;

import javax.annotation.Nullable;
import java.io.File;

public class GradleDistributionFactory {
    // Collect this early, as the process' current directory can change during embedded test execution
    private static final TestFile TEST_DIR = TestFile.of(new File("."));

    public static GradleDistribution distribution(String version) {
//        if (version.equals(getVersion().getVersion())) {
//            return new UnderDevelopmentGradleDistribution();
//        }
        TestFile previousVersionDir = getGradleUserHomeDir().getParentFile().file("previousVersion");
//        if (version.startsWith("#")) {
//            return new BuildServerGradleDistribution(version, previousVersionDir.file(version));
//        }
//
//        if (LocallyBuiltGradleDistribution.isLocallyBuiltVersion(version)) {
//            return new LocallyBuiltGradleDistribution(version);
//        }
        return new ReleasedGradleDistribution(version, previousVersionDir.file(version));
    }

    public static GradleDistribution wrapper(File rootProjectDirectory) {
        return new WrapperGradleDistribution(rootProjectDirectory);
    }

    public static GradleDistribution current() {
        return new CurrentGradleDistribution();
    }

    private static TestFile getGradleUserHomeDir() {
        return file("integTest.gradleUserHomeDir", "intTestHomeDir").file("worker-1");
    }

    protected static TestFile file(String propertyName, String defaultPath) {
        TestFile testFile = optionalFile(propertyName);
        if (testFile != null) {
            return testFile;
        }
        if (defaultPath == null) {
            throw new RuntimeException("You must set the '" + propertyName + "' property to run the integration tests.");
        }
        return testFile(defaultPath);
    }

    @Nullable
    private static TestFile optionalFile(String propertyName) {
        String path = System.getProperty(propertyName);
        // MODULE_WORKING_DIR doesn't seem to work correctly and MODULE_DIR seems to be in `.idea/modules/<path-to-subproject>`
        // See https://youtrack.jetbrains.com/issue/IDEA-194910
        return path != null ? new TestFile(new File(path.replace(".idea/modules/", ""))) : null;
    }

    private static TestFile testFile(String path) {
        File file = new File(path);
        return file.isAbsolute()
                ? new TestFile(file)
                : new TestFile(TEST_DIR.file(path).getAbsoluteFile());
    }
}
