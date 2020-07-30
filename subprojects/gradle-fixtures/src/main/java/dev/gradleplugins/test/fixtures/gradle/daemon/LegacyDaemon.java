package dev.gradleplugins.test.fixtures.gradle.daemon;

import lombok.val;
import lombok.var;
import org.gradle.internal.time.Time;
import org.gradle.launcher.daemon.server.api.DaemonStateControl;
import org.gradle.launcher.daemon.server.api.DaemonStateControl.State;
import org.gradle.util.GradleVersion;

import java.io.File;

public class LegacyDaemon extends AbstractDaemonFixture {
    private final DaemonLogFileStateProbe logFileProbe;

    public LegacyDaemon(File daemonLog, String version) {
        super(daemonLog);
        if (0 >= GradleVersion.version(version).getBaseVersion().compareTo(GradleVersion.version("2.2"))) {
            logFileProbe = new DaemonLogFileStateProbe(daemonLog, context);
        } else {
            logFileProbe = new DaemonLogFileStateProbe(daemonLog, context, "Daemon is busy, sleeping until state changes", "Daemon is idle, sleeping until state change");
        }
    }

    protected void waitForState(State state) throws InterruptedException {
        val timer = Time.startCountdownTimer(STATE_CHANGE_TIMEOUT);
        var lastLogState = logFileProbe.getCurrentState();
        while (!timer.hasExpired() && lastLogState != state) {
            Thread.sleep(200);
            lastLogState = logFileProbe.getCurrentState();
        }
        if (lastLogState == state) {
            return;
        }
        throw new AssertionError(String.format("Timeout waiting for daemon with pid %d to reach state %s.%nCurrent state is %s.", context.getPid(), state, lastLogState));
    }

    @Override
    protected void assertHasState(State state) {
        assert logFileProbe.getCurrentState() == state;
    }

    @Override
    public String getLog() {
        return logFileProbe.getLog();
    }

    @Override
    public File getLogFile() {
        return logFileProbe.getLogFile();
    }

    @Override
    public void changeTokenVisibleToClient() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void assertRegistryNotWorldReadable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPort() {
        throw new UnsupportedOperationException();
    }
}