package com.example;

import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VersionAwareTest {
    private static final String DEFAULT_GRADLE_VERSION_SYSPROP_NAME = "dev.gradleplugins.defaultGradleVersion";

    @BeforeEach
    void setup() {
        if (System.getProperties().containsKey(DEFAULT_GRADLE_VERSION_SYSPROP_NAME)) {
            System.out.println("Default Gradle version: " + getGradleDistributionUnderTest());
        } else {
            System.out.println("No default Gradle version");
        }
    }

    private static String getGradleDistributionUnderTest() {
        String defaultGradleVersionUnderTest = System.getProperty(DEFAULT_GRADLE_VERSION_SYSPROP_NAME, null);
        if (defaultGradleVersionUnderTest == null) {
            return GradleVersion.current().getVersion();
        }
        return defaultGradleVersionUnderTest;
    }

    @Test
    void printGradleVersionFromTest() {
        System.out.println("Using Gradle version: " + GradleVersion.current().getVersion());
    }
}