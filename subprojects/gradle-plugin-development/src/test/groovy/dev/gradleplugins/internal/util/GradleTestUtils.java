package dev.gradleplugins.internal.util;

import org.gradle.util.GradleVersion;

public final class GradleTestUtils {
    private GradleTestUtils() {}

    public static void setCurrentGradleVersion(GradleVersion version) {
        GradleUtils.CURRENT_GRADLE_VERSION = version;
    }
}
