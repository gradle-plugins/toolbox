package dev.gradleplugins.fixtures.gradle.runner.parameters;

import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.canonicalize;
import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SystemUtils.USER_NAME;

public final class GradleUserHomeDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionCommandLineParameter<File> {
    static final String TEST_KIT_DIR_SYS_PROP = "org.gradle.testkit.dir";

    public static GradleUserHomeDirectory unset() {
        return noValue(GradleUserHomeDirectory.class);
    }

    public static GradleUserHomeDirectory testKitDirectory() {
        if (System.getProperties().containsKey(TEST_KIT_DIR_SYS_PROP)) {
            return of(canonicalize(new File(System.getProperty(TEST_KIT_DIR_SYS_PROP))));
        } else {
            return of(file(SystemUtils.getJavaIoTmpDir(), ".gradle-test-kit-".concat(USER_NAME)));
        }
    }

    public static Function<GradleExecutionContext, File> relativeToGradleUserHome(String path) {
        return context -> file(context.getGradleUserHomeDirectory().get(), path);
    }

//    private File createTestKitDir(TestKitDirProvider testKitDirProvider) {
//        File dir = testKitDirProvider.getDir();
//        if (dir.isDirectory()) {
//            if (!dir.canWrite()) {
//                throw new InvalidRunnerConfigurationException("Unable to write to test kit directory: " + dir.getAbsolutePath());
//            }
//            return dir;
//        } else if (dir.exists() && !dir.isDirectory()) {
//            throw new InvalidRunnerConfigurationException("Unable to use non-directory as test kit directory: " + dir.getAbsolutePath());
//        } else if (dir.mkdirs() || dir.isDirectory()) {
//            return dir;
//        } else {
//            throw new InvalidRunnerConfigurationException("Unable to create test kit directory: " + dir.getAbsolutePath());
//        }
//    }

    public static GradleUserHomeDirectory of(File gradleUserHomeDirectory) {
        return fixed(GradleUserHomeDirectory.class, gradleUserHomeDirectory);
    }

    @Override
    public List<String> getAsArguments() {
        return map(GradleUserHomeDirectory::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File gradleUserHomeDirectory) {
        return asList("--gradle-user-home", gradleUserHomeDirectory.getAbsolutePath());
    }
}
