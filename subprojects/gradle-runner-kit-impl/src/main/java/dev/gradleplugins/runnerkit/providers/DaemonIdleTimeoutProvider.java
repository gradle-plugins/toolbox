package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException;

import java.time.Duration;
import java.util.Map;

import static java.util.Collections.singletonMap;

public final class DaemonIdleTimeoutProvider extends AbstractGradleExecutionProvider<Duration> implements GradleExecutionJvmSystemPropertyProvider {
    static final Duration DEFAULT_TEST_KIT_DAEMON_IDLE_TIMEOUT = Duration.ofSeconds(120);

    // See org.gradle.launcher.daemon.configuration.DaemonBuildOptions.DaemonBuildOptions.IdleTimeoutOption#GRADLE_PROPERTY
    static final String DAEMON_BUILD_OPTIONS_IDLE_TIMEOUT_OPTION_GRADLE_PROPERTY = "org.gradle.daemon.idletimeout";

    public static DaemonIdleTimeoutProvider testKitIdleTimeout() {
        return fixed(DaemonIdleTimeoutProvider.class, DEFAULT_TEST_KIT_DAEMON_IDLE_TIMEOUT);
    }

    public static DaemonIdleTimeoutProvider of(Duration daemonIdleTimeout) {
        return fixed(DaemonIdleTimeoutProvider.class, daemonIdleTimeout);
    }

    @Override
    public Map<String, String> getAsJvmSystemProperties() {
        return singletonMap(DAEMON_BUILD_OPTIONS_IDLE_TIMEOUT_OPTION_GRADLE_PROPERTY, String.valueOf(get().toMillis()));
    }

    @Override
    public void validate(GradleExecutionContext context) {
        if (context.getArguments().get().stream().anyMatch(it -> it.startsWith("-D" + DAEMON_BUILD_OPTIONS_IDLE_TIMEOUT_OPTION_GRADLE_PROPERTY))) {
            throw new InvalidRunnerConfigurationException("Please use GradleRunner#withDaemonIdleTimeout(Duration) instead of using the command line flags.");
        }
    }
}
