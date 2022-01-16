package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.gradle.util.VersionNumber;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class GradlePluginTestingStrategyFactoryInternal implements GradlePluginTestingStrategyFactory {
    private static final ReleasedVersionDistributions GRADLE_DISTRIBUTIONS = new ReleasedVersionDistributions();
    private final ReleasedVersionDistributions releasedVersions;
    private final Provider<String> minimumVersion;

    public GradlePluginTestingStrategyFactoryInternal(Provider<String> minimumVersion) {
        this(minimumVersion, GRADLE_DISTRIBUTIONS);
    }

    public GradlePluginTestingStrategyFactoryInternal(Provider<String> minimumVersion, ReleasedVersionDistributions releasedVersions) {
        this.minimumVersion = minimumVersion;
        this.releasedVersions = releasedVersions;
    }

    @Override
    public GradleVersionCoverageTestingStrategy getCoverageForMinimumVersion() {
        return new MinimumGradleVersionCoverageTestingStrategy();
    }

    @Override
    public GradleVersionCoverageTestingStrategy getCoverageForLatestNightlyVersion() {
        return new LatestNightlyGradleVersionCoverageTestingStrategy();
    }

    @Override
    public Provider<Set<GradleVersionCoverageTestingStrategy>> getCoverageForLatestGlobalAvailableVersionOfEachSupportedMajorVersions() {
        return minimumVersion.map(version -> {
            assertKnownMinimumVersion(version);
            val minimumMajorVersion = VersionNumber.parse(version).getMajor();
            val h = releasedVersions.getAllVersions().stream().filter(it -> !it.isSnapshot() && !it.getVersion().contains("-rc-")).map(it -> VersionNumber.parse(it.getVersion())).filter(it -> it.getMajor() >= minimumMajorVersion).collect(
            Collectors.groupingBy(it -> it.getMajor()));
            return h.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).map(it -> {
                it.sort(Comparator.reverseOrder());
                return coverageForGradleVersion(format(it.iterator().next()));
            }).collect(Collectors.toCollection(LinkedHashSet::new));
        });
    }

    private static String format(VersionNumber version) {
        val builder = new StringBuilder();
        builder.append(version.getMajor()).append(".").append(version.getMinor());
        if (version.getMicro() > 0) {
            builder.append(".").append(version.getMicro());
        }
        return builder.toString();
    }

    @Override
    public GradleVersionCoverageTestingStrategy getCoverageForLatestGlobalAvailableVersion() {
        return new LatestGlobalAvailableGradleVersionCoverageTestingStrategy();
    }

    @Override
    public GradleVersionCoverageTestingStrategy coverageForGradleVersion(String version) {
        if (!isKnownVersion(version)) {
            throw new IllegalArgumentException(String.format("Unknown Gradle version '%s' for adhoc testing strategy.", version));
        }
        return new AdhocGradleVersionCoverageTestingStrategy(version);
    }

    private boolean isKnownVersion(String version) {
        return releasedVersions.getAllVersions().stream().anyMatch(it -> it.getVersion().equals(version));
    }

    private void assertKnownMinimumVersion(String version) {
        if (!isKnownVersion(version)) {
            throw new IllegalArgumentException(String.format("Unknown minimum Gradle version '%s' for testing strategy.", version));
        }
    }

    private abstract class AbstractGradleVersionCoverageTestingStrategy implements GradlePluginTestingStrategyInternal, GradleVersionCoverageTestingStrategy {
        private final String name;
        private final Supplier<String> versionSupplier;

        AbstractGradleVersionCoverageTestingStrategy(String name, Supplier<String> versionSupplier) {
            this.name = name;
            this.versionSupplier = versionSupplier;
        }

        @Override
        public boolean isLatestGlobalAvailable() {
            return releasedVersions.getMostRecentRelease().getVersion().equals(getVersion());
        }

        @Override
        public boolean isLatestNightly() {
            return getVersion().contains("-") && releasedVersions.getMostRecentSnapshot().getVersion().equals(getVersion());
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return versionSupplier.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof AbstractGradleVersionCoverageTestingStrategy)) {
                return false;
            }
            AbstractGradleVersionCoverageTestingStrategy that = (AbstractGradleVersionCoverageTestingStrategy) o;
            return Objects.equals(getVersion(), that.getVersion());// && Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getName());
        }

        @Override
        public String toString() {
            return "coverage for Gradle v" + getVersion();
        }
    }

    private final class MinimumGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {
        MinimumGradleVersionCoverageTestingStrategy() {
            super(MINIMUM_GRADLE, () -> {
                val result = minimumVersion.get();
                assertKnownMinimumVersion(result);
                return result;
            });
        }
    }

    private final class LatestNightlyGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {
        LatestNightlyGradleVersionCoverageTestingStrategy() {
            super(LATEST_NIGHTLY, () -> releasedVersions.getMostRecentSnapshot().getVersion());
        }
    }

    private final class LatestGlobalAvailableGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {

        LatestGlobalAvailableGradleVersionCoverageTestingStrategy() {
            super(LATEST_GLOBAL_AVAILABLE, () -> releasedVersions.getMostRecentRelease().getVersion());
        }
    }

    private final class AdhocGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {
        private AdhocGradleVersionCoverageTestingStrategy(String version) {
            super(version, () -> version);
        }
    }
}
