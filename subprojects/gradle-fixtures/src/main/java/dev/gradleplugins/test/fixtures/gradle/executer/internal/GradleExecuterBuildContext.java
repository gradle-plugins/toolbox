package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import java.io.File;

@Deprecated
public interface GradleExecuterBuildContext {
    File getGradleHomeDirectory();

    File getDaemonBaseDirectory();

    File getGradleUserHomeDirectory();

    File getTemporaryDirectory();
}
