package dev.gradleplugins.test.fixtures.gradle.daemon;

import org.gradle.internal.time.CountdownTimer;
import org.gradle.internal.time.Time;
import org.gradle.launcher.daemon.registry.DaemonRegistry;
import org.gradle.launcher.daemon.server.api.DaemonStateControl;
import org.gradle.launcher.daemon.server.api.DaemonStateControl.State;

import java.io.File;

public class TestableDaemon extends AbstractDaemonFixture {
    private final DaemonLogFileStateProbe logFileProbe;
    private final DaemonRegistryStateProbe registryProbe;

    public TestableDaemon(File daemonLog, DaemonRegistry registry) {
        super(daemonLog);
        this.logFileProbe = new DaemonLogFileStateProbe(daemonLog, context);
        this.registryProbe = new DaemonRegistryStateProbe(registry, context);
    }

    protected void waitForState(State state) throws InterruptedException {
        final CountdownTimer timer = Time.startCountdownTimer(STATE_CHANGE_TIMEOUT);
        DaemonStateControl.State lastRegistryState = registryProbe.getCurrentState();
        State lastLogState = logFileProbe.getCurrentState();
        while (!timer.hasExpired() && (lastRegistryState != state || lastLogState != state)) {
            Thread.sleep(200);
            lastRegistryState = registryProbe.getCurrentState();
            lastLogState = logFileProbe.getCurrentState();
        }
        if (lastRegistryState == state && lastLogState == state) {
            return;
        }
        throw new AssertionError(String.format("Timeout waiting for daemon with pid %d to reach state %s.%nCurrent registry state is %s and current log state is %s.", context.getPid(), state, lastRegistryState, lastLogState));
    }

    @Override
    protected void assertHasState(State state) {
        assert logFileProbe.getCurrentState() == state;
        assert registryProbe.getCurrentState() == state;
    }

    @Override
    public void assertRegistryNotWorldReadable() {
        registryProbe.assertRegistryNotWorldReadable();
    }

    @Override
    public void changeTokenVisibleToClient() {
        registryProbe.resetToken();
    }

    public String getLog() {
        return logFileProbe.getLog();
    }

    @Override
    public File getLogFile() {
        return logFileProbe.getLogFile();
    }

    public int getPort() {
        return logFileProbe.getPort();
    }
}
