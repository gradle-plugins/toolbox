package dev.gradleplugins.test.fixtures.gradle.daemon;

import org.gradle.launcher.daemon.context.DaemonContext;

import java.io.File;

public interface DaemonFixture {
    /**
     * Returns the context information of this daemon.
     */
    DaemonContext getContext();

    /**
     * Returns the log for this daemon.
     */
    String getLog();

    /**
     * Returns the log file for this daemon.
     */
    File getLogFile();

    /**
     * Returns whether the log file contains a given String.
     *
     * Works without reading the whole log file into memory.
     */
    boolean logContains(String searchString);

    /**
     * Returns whether the log file contains a given String, starting from line `fromLine`
     *
     * The first line in the file is the line 0.
     *
     * Works without reading the whole log file into memory.
     */
    boolean logContains(long fromLine, String searchString);

    /**
     * Returns the TCP port used by this daemon.
     */
    int getPort();

    /**
     * Forcefully kills this daemon and all child processes.
     */
    void kill();

    /**
     * Forcefully kills this daemon, but not child processes.
     */
    void killDaemonOnly();

    /**
     * Changes the authentication token for this daemon in the registry, so that client will see a different token to that expected by this daemon
     */
    void changeTokenVisibleToClient();

    void assertRegistryNotWorldReadable();

    /**
     * Asserts that this daemon becomes idle within a short timeout. Blocks until this has happened.
     */
    void becomesIdle();

    /**
     * Asserts that this daemon stops and is no longer visible to any clients within a short timeout. Blocks until this has happened.
     */
    void stops();

    /**
     * Asserts that this daemon is currently idle.
     */
    void assertIdle();

    /**
     * Asserts that this daemon is currently busy.
     */
    void assertBusy();

    /**
     * Asserts that this daemon is in a canceled state.
     */
    void assertCanceled();

    /**
     * Asserts that this daemon becomes canceled within a short timeout. Blocks until this has happened.
     */
    void becomesCanceled();

    /**
     * Asserts that this daemon has stopped and is no longer visible to any clients.
     */
    void assertStopped();
}