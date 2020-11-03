package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.singletonMap;

public final class DaemonBaseDirectoryProvider extends AbstractGradleExecutionProvider<File> implements GradleExecutionJvmSystemPropertyProvider {
    // See org.gradle.testkit.runner.internal.ToolingApiGradleExecutor#TEST_KIT_DAEMON_DIR_NAME
    static final String TEST_KIT_DAEMON_DIR_NAME = "test-kit-daemon";

    // See org.gradle.launcher.daemon.configuration.DaemonBuildOptions.BaseDirOption#GRADLE_PROPERTY
    static final String DAEMON_BUILD_OPTIONS_BASE_DIR_GRADLE_PROPERTY = "org.gradle.daemon.registry.base";

    public static DaemonBaseDirectoryProvider of(File daemonBaseDirectory) {
        return fixed(DaemonBaseDirectoryProvider.class, daemonBaseDirectory);
    }

    public static DaemonBaseDirectoryProvider of(Function<GradleExecutionContext, File> daemonBaseDirectorySupplier) {
        return calculated(DaemonBaseDirectoryProvider.class, daemonBaseDirectorySupplier);
    }

    public static DaemonBaseDirectoryProvider testKitDaemonDirectory() {
        return calculated(DaemonBaseDirectoryProvider.class, GradleUserHomeDirectoryProvider.relativeToGradleUserHome(TEST_KIT_DAEMON_DIR_NAME));
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(DaemonBaseDirectoryProvider::asJvmSystemProperties).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> asJvmSystemProperties(File daemonBaseDirectory) {
        return singletonMap(DAEMON_BUILD_OPTIONS_BASE_DIR_GRADLE_PROPERTY, daemonBaseDirectory.getAbsolutePath());
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("-D" + DAEMON_BUILD_OPTIONS_BASE_DIR_GRADLE_PROPERTY))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withDaemonBaseDirectory(File) instead of using the command line flags.");
        }
    }
}
