package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public final class DaemonIdleTimeoutParameter extends GradleExecutionParameterImpl<Duration> implements JvmSystemPropertyParameter<Duration>, GradleExecutionParameter<Duration> {
    public static DaemonIdleTimeoutParameter of(Duration daemonIdleTimeout) {
        return fixed(DaemonIdleTimeoutParameter.class, daemonIdleTimeout);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return Collections.singletonMap(DaemonBuildOptions.IdleTimeoutOption.GRADLE_PROPERTY, String.valueOf(get().toMillis()));
    }
}
