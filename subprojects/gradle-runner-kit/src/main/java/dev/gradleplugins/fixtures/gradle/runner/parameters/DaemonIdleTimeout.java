package dev.gradleplugins.fixtures.gradle.runner.parameters;

import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;

import java.time.Duration;
import java.util.Map;

import static java.util.Collections.singletonMap;

public final class DaemonIdleTimeout extends GradleExecutionParameterImpl<Duration> implements GradleExecutionJvmSystemPropertyParameter<Duration> {
    public static DaemonIdleTimeout of(Duration daemonIdleTimeout) {
        return fixed(DaemonIdleTimeout.class, daemonIdleTimeout);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return singletonMap(DaemonBuildOptions.IdleTimeoutOption.GRADLE_PROPERTY, String.valueOf(get().toMillis()));
    }
}
