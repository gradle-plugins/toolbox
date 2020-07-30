package dev.gradleplugins.test.fixtures.gradle.daemon;

import dev.gradleplugins.test.fixtures.ProcessFixture;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.launcher.daemon.context.DaemonContext;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static org.gradle.launcher.daemon.server.api.DaemonStateControl.State;
import static org.gradle.launcher.daemon.server.api.DaemonStateControl.State.*;

public abstract class AbstractDaemonFixture implements DaemonFixture {
    public static final int STATE_CHANGE_TIMEOUT = 20000;
    public final DaemonContext context;

    public AbstractDaemonFixture(File daemonLog) {
        this.context = DaemonContextParser.parseFromFile(daemonLog);
        if (this.context.getPid() == null) {
            try {
                System.out.println(String.format("PID in daemon log (%s) is null.", daemonLog.getAbsolutePath()));
                System.out.println(String.format("daemon.log exists: %s", daemonLog.exists()));

                System.out.println("start daemon.log content: ");
                val daemonLogContent = FileUtils.readFileToString(daemonLog, Charset.defaultCharset());
                System.out.println(String.format("{daemonLog.text.isEmpty()} = %s", daemonLogContent.isEmpty()));
                System.out.println(daemonLogContent);
                System.out.println("end daemon.log content");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public DaemonContext getContext() {
        return context;
    }

    @Override
    public boolean logContains(String searchString) {
        return logContains(0, searchString);
    }

    @SneakyThrows
    @Override
    public boolean logContains(long fromLine, String searchString) {
        return Files.lines(getLogFile().toPath()).skip(fromLine).anyMatch(it -> it.contains(searchString));
    }

    @SneakyThrows
    public void becomesIdle() {
        waitForState(Idle);
    }

    @SneakyThrows
    public void stops() {
        waitForState(Stopped);
    }

    @Override
    public void assertIdle() {
        assertHasState(Idle);
    }

    @Override
    public void assertBusy() {
        assertHasState(Busy);
    }

    @Override
    public void assertStopped() {
        assertHasState(Stopped);
    }

    @Override
    public void assertCanceled() {
        assertHasState(Canceled);
    }

    @SneakyThrows
    @Override
    public void becomesCanceled() {
        waitForState(Canceled);
    }

    protected abstract void waitForState(State state) throws InterruptedException;

    protected abstract void assertHasState(State state);

    @Override
    public void kill() {
        new ProcessFixture(context.getPid()).kill(true);
    }

    @Override
    public void killDaemonOnly() {
        new ProcessFixture(context.getPid()).kill(false);
    }

    @Override
    public String toString() {
        return "Daemon with context " + context;
    }
}