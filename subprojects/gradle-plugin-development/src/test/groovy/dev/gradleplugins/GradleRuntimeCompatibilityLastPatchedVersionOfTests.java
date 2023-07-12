package dev.gradleplugins;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static dev.gradleplugins.GradleRuntimeCompatibility.lastPatchedVersionOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GradleRuntimeCompatibilityLastPatchedVersionOfTests {
    @ParameterizedTest
    @CsvSource({
            // According to https://gradle.org/releases/
            "0.7,       0.7",
            "0.8,       0.8",
            "0.9,       0.9.2",
            "0.9.1,     0.9.2",
            "0.9.2,     0.9.2",
            "1.0,       1.0",
            "1.1,       1.1",
            "1.2,       1.2",
            "1.3,       1.3",
            "1.4,       1.4",
            "1.5,       1.5",
            "1.6,       1.6",
            "1.7,       1.7",
            "1.8,       1.8",
            "1.9,       1.9",
            "1.10,      1.10",
            "1.11,      1.11",
            "1.12,      1.12",
            "2.0,       2.0",
            "2.1,       2.1",
            "2.2,       2.2.1",
            "2.2.1,     2.2.1",
            "2.3,       2.3",
            "2.4,       2.4",
            "2.5,       2.5",
            "2.6,       2.6",
            "2.7,       2.7",
            "2.8,       2.8",
            "2.9,       2.9",
            "2.10,      2.10",
            "2.11,      2.11",
            "2.12,      2.12",
            "2.13,      2.13",
            "2.14,      2.14.1",
            "2.14.1,    2.14.1",
            "3.0,       3.0",
            "3.1,       3.1",
            "3.2,       3.2.1",
            "3.2.1,     3.2.1",
            "3.3,       3.3",
            "3.4,       3.4.1",
            "3.4.1,     3.4.1",
            "3.5,       3.5.1",
            "4.0,       4.0.2",
            "3.5.1,     3.5.1",
            "4.0.1,     4.0.2",
            "4.0.2,     4.0.2",
            "4.1,       4.1",
            "4.2,       4.2.1",
            "4.2.1,     4.2.1",
            "4.3,       4.3.1",
            "4.3.1,     4.3.1",
            "4.4,       4.4.1",
            "4.4.1,     4.4.1",
            "4.5,       4.5.1",
            "4.5.1,     4.5.1",
            "4.6,       4.6",
            "4.7,       4.7",
            "4.8,       4.8.1",
            "4.8.1,     4.8.1",
            "4.9,       4.9",
            "4.10,      4.10.3",
            "4.10.1,    4.10.3",
            "4.10.2,    4.10.3",
            "5.0,       5.0",
            "4.10.3,    4.10.3",
            "5.1,       5.1.1",
            "5.1.1,     5.1.1",
            "5.2,       5.2.1",
            "5.2.1,     5.2.1",
            "5.3,       5.3.1",
            "5.3.1,     5.3.1",
            "5.4,       5.4.1",
            "5.4.1,     5.4.1",
            "5.5,       5.5.1",
            "5.5.1,     5.5.1",
            "5.6,       5.6.4",
            "5.6.1,     5.6.4",
            "5.6.2,     5.6.4",
            "5.6.3,     5.6.4",
            "5.6.4,     5.6.4",
            "6.0,       6.0.1",
            "6.0.1,     6.0.1",
            "6.1,       6.1.1",
            "6.1.1,     6.1.1",
            "6.2,       6.2.2",
            "6.2.1,     6.2.2",
            "6.2.2,     6.2.2",
            "6.3,       6.3",
            "6.4,       6.4.1",
            "6.4.1,     6.4.1",
            "6.5,       6.5.1",
            "6.5.1,     6.5.1",
            "6.6,       6.6.1",
            "6.6.1,     6.6.1",
            "6.7,       6.7.1",
            "6.7.1,     6.7.1",
            "6.8,       6.8.3",
            "6.8.1,     6.8.3",
            "6.8.2,     6.8.3",
            "6.8.3,     6.8.3",
            "7.0,       7.0.2",
            "6.9,       6.9.4",
            "7.0.1,     7.0.2",
            "7.0.2,     7.0.2",
            "7.1,       7.1.1",
            "7.2,       7.2",
            "6.9.1,     6.9.4",
            "7.3,       7.3.3",
            "7.3.1,     7.3.3",
            "7.3.2,     7.3.3",
            "6.9.2,     6.9.4",
            "7.3.3,     7.3.3",
            "7.4,       7.4.2",
            "7.4.1,     7.4.2",
            "7.4.2,     7.4.2",
            "7.5,       7.5.1",
            "7.5.1,     7.5.1",
            "6.9.3,     6.9.4",
            "7.6,       7.6.2",
            "8.0,       8.0.2",
            "8.0.1,     8.0.2",
            "6.9.4,     6.9.4",
            "7.6.1,     7.6.2",
            "8.0.2,     8.0.2",
            "8.1,       8.1.1",
            "8.1.1,     8.1.1",
            "7.6.2,     7.6.2",
            "8.2,       8.2.1",
            "8.2.1,     8.2.1",
    })
    void checkLastPatchedGradleVersion(String validGradleVersion, String expectedLastPatchedGradleVersion) {
        assertThat(lastPatchedVersionOf(validGradleVersion), equalTo(expectedLastPatchedGradleVersion));
    }
}
