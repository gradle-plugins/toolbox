package dev.gradleplugins.test.fixtures.gradle.daemon;

import java.io.File;
import java.util.List;

public interface DaemonsFixture {
    /**
     * Kills all daemons.
     */
    void killAll();

    /**
     * Returns all known daemons. Includes any daemons that are no longer running.
     */
    List<? extends DaemonFixture> getDaemons();

    /**
     * Returns all daemons that are visible to clients. May include daemons that are no longer running (eg they have crashed).
     */
    List<? extends DaemonFixture> getVisible();

    /**
     * Convenience to get a single daemon. Fails if there is not exactly 1 daemon.
     */
    DaemonFixture getDaemon();

    /**
     * Returns the base dir of the daemon.
     */
    File getDaemonBaseDir();

    /**
     * Returns the Gradle version of the daemon.
     */
    String getVersion();
}