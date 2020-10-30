package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.fixtures.gradle.runner.GradleExecutionContext;
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions.BaseDirOption;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

public final class DaemonBaseDirectory extends GradleExecutionParameterImpl<File> implements GradleExecutionJvmSystemPropertyParameter<File> {
    public static DaemonBaseDirectory unset() {
        return noValue(DaemonBaseDirectory.class);
    }

    public static DaemonBaseDirectory of(File daemonBaseDirectory) {
        return fixed(DaemonBaseDirectory.class, daemonBaseDirectory);
    }

    public static DaemonBaseDirectory of(Function<GradleExecutionContext, File> daemonBaseDirectorySupplier) {
        return calculated(DaemonBaseDirectory.class, daemonBaseDirectorySupplier);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return map(DaemonBaseDirectory::asJvmSystemProperties).orElseGet(ImmutableMap::of);
    }

    private static Map<String, String> asJvmSystemProperties(File daemonBaseDirectory) {
        return ImmutableMap.of(BaseDirOption.GRADLE_PROPERTY, daemonBaseDirectory.getAbsolutePath());
    }
}
