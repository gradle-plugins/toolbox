package dev.gradleplugins.test.fixtures.gradle.daemon;

import org.gradle.launcher.daemon.server.api.DaemonStateControl;

public interface DaemonStateProbe {
    DaemonStateControl.State getCurrentState();
}