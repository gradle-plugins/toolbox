package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;

import java.util.Collections;
import java.util.Map;

@Deprecated
public final class DaemonBaseDirectoryParameter extends GradleExecutionParameterImpl<DaemonBaseDirectory> implements JvmSystemPropertyParameter<DaemonBaseDirectory>, DirectoryParameter<DaemonBaseDirectory> {
    public static DaemonBaseDirectoryParameter unset() {
        return noValue(DaemonBaseDirectoryParameter.class);
    }

    public static DaemonBaseDirectoryParameter of(DaemonBaseDirectory daemonBaseDirectory) {
        return fixed(DaemonBaseDirectoryParameter.class, daemonBaseDirectory);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        if (isPresent()) {
            return Collections.singletonMap(DaemonBuildOptions.BaseDirOption.GRADLE_PROPERTY, get().getAbsolutePath());
        }
        return Collections.emptyMap();
    }
}
