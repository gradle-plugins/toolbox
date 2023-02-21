package dev.gradleplugins;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static dev.gradleplugins.GradleRuntimeCompatibility.lastMinorReleaseOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class GradleRuntimeCompatibilityLastMinorReleaseOfTests {
    @ParameterizedTest
    @CsvSource({
            // According to https://gradle.org/releases/
            "0.7,       0.9.2",
            "0.8,       0.9.2",
            "0.9,       0.9.2",
            "0.9.1,     0.9.2",
            "0.9.2,     0.9.2",
            "1.0,       1.14",
            "1.1,       1.14",
            "1.2,       1.14",
            "1.3,       1.14",
            "1.4,       1.14",
            "1.5,       1.14",
            "1.6,       1.14",
            "1.7,       1.14",
            "1.8,       1.14",
            "1.9,       1.14",
            "1.10,      1.14",
            "1.11,      1.14",
            "1.12,      1.14",
            "2.0,       2.14.1",
            "2.1,       2.14.1",
            "2.2,       2.14.1",
            "2.2.1,     2.14.1",
            "2.3,       2.14.1",
            "2.4,       2.14.1",
            "2.5,       2.14.1",
            "2.6,       2.14.1",
            "2.7,       2.14.1",
            "2.8,       2.14.1",
            "2.9,       2.14.1",
            "2.10,      2.14.1",
            "2.11,      2.14.1",
            "2.12,      2.14.1",
            "2.13,      2.14.1",
            "2.14,      2.14.1",
            "2.14.1,    2.14.1",
            "3.0,       3.5.1",
            "3.1,       3.5.1",
            "3.2,       3.5.1",
            "3.2.1,     3.5.1",
            "3.3,       3.5.1",
            "3.4,       3.5.1",
            "3.4.1,     3.5.1",
            "3.5,       3.5.1",
            "4.0,       4.10.3",
            "3.5.1,     3.5.1",
            "4.0.1,     4.10.3",
            "4.0.2,     4.10.3",
            "4.1,       4.10.3",
            "4.2,       4.10.3",
            "4.2.1,     4.10.3",
            "4.3,       4.10.3",
            "4.3.1,     4.10.3",
            "4.4,       4.10.3",
            "4.4.1,     4.10.3",
            "4.5,       4.10.3",
            "4.5.1,     4.10.3",
            "4.6,       4.10.3",
            "4.7,       4.10.3",
            "4.8,       4.10.3",
            "4.8.1,     4.10.3",
            "4.9,       4.10.3",
            "4.10,      4.10.3",
            "4.10.1,    4.10.3",
            "4.10.2,    4.10.3",
            "5.0,       5.6.4",
            "4.10.3,    4.10.3",
            "5.1,       5.6.4",
            "5.1.1,     5.6.4",
            "5.2,       5.6.4",
            "5.2.1,     5.6.4",
            "5.3,       5.6.4",
            "5.3.1,     5.6.4",
            "5.4,       5.6.4",
            "5.4.1,     5.6.4",
            "5.5,       5.6.4",
            "5.5.1,     5.6.4",
            "5.6,       5.6.4",
            "5.6.1,     5.6.4",
            "5.6.2,     5.6.4",
            "5.6.3,     5.6.4",
            "5.6.4,     5.6.4",
            "6.0,       6.9.3",
            "6.0.1,     6.9.3",
            "6.1,       6.9.3",
            "6.1.1,     6.9.3",
            "6.2,       6.9.3",
            "6.2.1,     6.9.3",
            "6.2.2,     6.9.3",
            "6.3,       6.9.3",
            "6.4,       6.9.3",
            "6.4.1,     6.9.3",
            "6.5,       6.9.3",
            "6.5.1,     6.9.3",
            "6.6,       6.9.3",
            "6.6.1,     6.9.3",
            "6.7,       6.9.3",
            "6.7.1,     6.9.3",
            "6.8,       6.9.3",
            "6.8.1,     6.9.3",
            "6.8.2,     6.9.3",
            "6.8.3,     6.9.3",
            "7.0,       7.6",
            "6.9,       6.9.3",
            "7.0.1,     7.6",
            "7.0.2,     7.6",
            "7.1,       7.6",
            "7.2,       7.6",
            "6.9.1,     6.9.3",
            "7.3,       7.6",
            "7.3.1,     7.6",
            "7.3.2,     7.6",
            "6.9.2,     6.9.3",
            "7.3.3,     7.6",
            "7.4,       7.6",
            "7.4.1,     7.6",
            "7.4.2,     7.6",
            "7.5,       7.6",
            "7.5.1,     7.6",
            "6.9.3,     6.9.3",
            "7.6,       7.6",
            "8.0,       8.0.1",
            "8.0.1,     8.0.1"
    })
    void checkLastMinorReleasedGradleVersion(String validGradleVersion, String expectedLastMinorGradleVersion) {
        assertThat(lastMinorReleaseOf(validGradleVersion), equalTo(expectedLastMinorGradleVersion));
    }
}
