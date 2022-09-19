package dev.gradleplugins.internal.util;

import org.gradle.util.GradleVersion;

public final class GradleUtils {
    /* visible for testing */ static GradleVersion CURRENT_GRADLE_VERSION = GradleVersion.current();

    private GradleUtils() {}

    public static GradleVersion currentGradleVersion() {
        return CURRENT_GRADLE_VERSION;
    }
}
