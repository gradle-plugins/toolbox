package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.canonicalize;
import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.SystemUtils.USER_NAME;

public final class GradleUserHomeDirectoryProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionCommandLineProvider {
    // See org.gradle.testkit.runner.internal.DefaultGradleRunner#TEST_KIT_DIR_SYS_PROP
    static final String TEST_KIT_DIR_SYS_PROP = "org.gradle.testkit.dir";

    public static GradleUserHomeDirectoryProvider testKitDirectory() {
        if (System.getProperties().containsKey(TEST_KIT_DIR_SYS_PROP)) {
            return of(canonicalize(new File(System.getProperty(TEST_KIT_DIR_SYS_PROP))));
        } else {
            return of(file(SystemUtils.getJavaIoTmpDir(), ".gradle-test-kit-".concat(USER_NAME)));
        }
    }

    public static Function<GradleExecutionContext, File> relativeToGradleUserHome(String path) {
        return context -> {
            ((GradleExecutionProviderInternal<File>) context.getGradleUserHomeDirectory()).calculateValue(context);
            return FileSystemUtils.file(context.getGradleUserHomeDirectory().get(), path);
        };
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

    public static GradleUserHomeDirectoryProvider of(File gradleUserHomeDirectory) {
        return fixed(GradleUserHomeDirectoryProvider.class, gradleUserHomeDirectory);
    }

    public static GradleUserHomeDirectoryProvider isolatedGradleUserHomeDirectory() {
        return calculated(GradleUserHomeDirectoryProvider.class, WorkingDirectoryProvider.relativeToWorkingDirectory("user-home"));
    }

    @Override
    public List<String> getAsArguments() {
        return map(GradleUserHomeDirectoryProvider::asArguments).orElseGet(Collections::emptyList);
    }

    private static List<String> asArguments(File gradleUserHomeDirectory) {
        return asList("--gradle-user-home", gradleUserHomeDirectory.getAbsolutePath());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("--gradle-user-home") || it.equals("-g"))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withUserHomeDirectory(File) instead of using the command line flags.");
        }
    }
}
