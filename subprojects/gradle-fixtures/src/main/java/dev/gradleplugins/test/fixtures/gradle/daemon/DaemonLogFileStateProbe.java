package dev.gradleplugins.test.fixtures.gradle.daemon;

import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.gradle.launcher.daemon.context.DaemonContext;
import org.gradle.launcher.daemon.logging.DaemonMessages;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.gradle.launcher.daemon.server.api.DaemonStateControl.State;
import static org.gradle.launcher.daemon.server.api.DaemonStateControl.State.*;
import static org.junit.Assert.assertTrue;

public class DaemonLogFileStateProbe implements DaemonStateProbe {
    private final DaemonContext context;
    private final File log;
    private final String startBuildMessage;
    private final String finishBuildMessage;

    public DaemonLogFileStateProbe(File daemonLog, DaemonContext context) {
        this(daemonLog, context, DaemonMessages.STARTED_BUILD);
    }

    public DaemonLogFileStateProbe(File daemonLog, DaemonContext context, String startBuildMessage) {
        this(daemonLog, context, startBuildMessage, DaemonMessages.FINISHED_BUILD);
    }

    public DaemonLogFileStateProbe(File daemonLog, DaemonContext context, String startBuildMessage, String finishBuildMessage) {
        this.finishBuildMessage = finishBuildMessage;
        this.startBuildMessage = startBuildMessage;
        this.log = daemonLog;
        this.context = context;
    }

    @Override
    public String toString() {
        return String.format("DaemonLogFile{file: %s, context: %s}", log, context);
    }

    public DaemonContext getContext() {
        return context;
    }

    public State getCurrentState() {
        return Iterables.getLast(getStates());
    }

    public List<State> getStates() {
        List<State> states = new LinkedList<>();
        states.add(Idle);
        try {
            FileUtils.readLines(log, Charset.defaultCharset()).forEach(it -> {
                if (it.contains(startBuildMessage)) {
                    states.add(Busy);
                } else if (it.contains(finishBuildMessage)) {
                    states.add(Idle);
                } else if (it.contains(DaemonMessages.CANCELED_BUILD)) {
                    states.add(Canceled);
                } else if (it.contains(DaemonMessages.DAEMON_VM_SHUTTING_DOWN)) {
                    states.add(Stopped);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return states;
    }

    public String getLog() {
        try {
            return FileUtils.readFileToString(log, Charset.defaultCharset());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public File getLogFile() {
        return log;
    }

    public int getPort() {
        Pattern pattern = Pattern.compile("^.*" + DaemonMessages.ADVERTISING_DAEMON + ".*port:(\\d+).*",
                Pattern.MULTILINE + Pattern.DOTALL);

        Matcher matcher = pattern.matcher(getLog());
        assertTrue("Unable to find daemon address in the daemon log. Daemon: " + context, matcher.matches());

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Unexpected format of the port number found in the daemon log. Daemon: " + context);
        }
    }
}