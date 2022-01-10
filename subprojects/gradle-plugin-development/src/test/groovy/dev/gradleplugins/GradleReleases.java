package dev.gradleplugins;

import dev.gradleplugins.internal.ReleasedVersionDistributions;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public final class GradleReleases {
    private static final DateTimeFormatter SNAPSHOT_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .appendValue(HOUR_OF_DAY, 2)
            .appendValue(MINUTE_OF_HOUR, 2)
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendOffset("+HHMM", "-0000")
            .toFormatter();
    private static long rcCount = 0;

    public static ReleasedVersionDistributions.GradleRelease snapshotFor(String version) {
        assert !version.contains("-") : "'version' must be a GA version";
        return new ReleasedVersionDistributions.GradleRelease(version + "-" + SNAPSHOT_DATE_FORMATTER.format(ZonedDateTime.now()), true, false, "");
    }

    public static ReleasedVersionDistributions.GradleRelease releaseCandidateFor(String version) {
        assert !version.contains("-") : "'version' must be a GA version";
        return new ReleasedVersionDistributions.GradleRelease(version + "-rc-" + ++rcCount, false, false, version);
    }

    public static ReleasedVersionDistributions.GradleRelease globalAvailable(String version) {
        assert !version.contains("-") : "'version' must be a GA version";
        return new ReleasedVersionDistributions.GradleRelease(version, false, false, "");
    }

    public static ReleasedVersionDistributions.GradleRelease current(ReleasedVersionDistributions.GradleRelease release) {
        assert !release.isSnapshot();
        assert !release.getVersion().contains("-rc-");
        return new ReleasedVersionDistributions.GradleRelease(release.getVersion(), false, true, "");
    }
}
