package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.Value;
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Value
public class DaemonIdleTimeoutParameter implements JvmSystemPropertyParameter {
    Duration value;

    public static DaemonIdleTimeoutParameter of(Duration daemonIdleTimeout) {
        return new DaemonIdleTimeoutParameter(daemonIdleTimeout);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return Collections.singletonMap(DaemonBuildOptions.IdleTimeoutOption.GRADLE_PROPERTY, String.valueOf(value.toMillis()));
    }
}
