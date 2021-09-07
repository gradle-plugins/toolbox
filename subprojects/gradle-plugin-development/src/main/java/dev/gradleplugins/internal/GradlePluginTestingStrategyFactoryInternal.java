package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import java.util.Objects;

public final class GradlePluginTestingStrategyFactoryInternal implements GradlePluginTestingStrategyFactory {
    private final ReleasedVersionDistributions releasedVersions = new ReleasedVersionDistributions();
    private final Provider<String> minimumVersion;

    public GradlePluginTestingStrategyFactoryInternal(Provider<String> minimumVersion) {
        this.minimumVersion = minimumVersion;
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
    public GradleVersionCoverageTestingStrategy getCoverageForLatestGlobalAvailableVersion() {
        return new LatestGlobalAvailableGradleVersionCoverageTestingStrategy();
    }

    @Override
    public GradleVersionCoverageTestingStrategy coverageForGradleVersion(String version) {
        if (new ReleasedVersionDistributions().getAllVersions().stream().noneMatch(it -> it.getVersion().equals(version))) {
            throw new RuntimeException(String.format("Unknown Gradle version '%s' for adhoc testing strategy.", version));
        }
        return new AdhocGradleVersionCoverageTestingStrategy(version);
    }

    private abstract class AbstractGradleVersionCoverageTestingStrategy implements GradlePluginTestingStrategyInternal, GradleVersionCoverageTestingStrategy {
        @Override
        public boolean isLatestGlobalAvailable() {
            return releasedVersions.getMostRecentRelease().getVersion().equals(getVersion());
        }

        @Override
        public boolean isLatestNightly() {
            return releasedVersions.getMostRecentSnapshot().getVersion().equals(getVersion());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof AbstractGradleVersionCoverageTestingStrategy)) {
                return false;
            }
            AbstractGradleVersionCoverageTestingStrategy that = (AbstractGradleVersionCoverageTestingStrategy) o;
            return Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getName(), that.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getVersion(), getName());
        }
    }

    private final class MinimumGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {
        @Override
        public String getName() {
            return MINIMUM_GRADLE;
        }

        @Override
        public String getVersion() {
            return minimumVersion.get();
        }
    }

    private final class LatestNightlyGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {
        @Override
        public String getName() {
            return LATEST_NIGHTLY;
        }

        @Override
        public String getVersion() {
            return releasedVersions.getMostRecentSnapshot().getVersion();
        }

        @Override
        public boolean isLatestGlobalAvailable() {
            return false;
        }

        @Override
        public boolean isLatestNightly() {
            return true;
        }
    }

    private final class LatestGlobalAvailableGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {

        @Override
        public String getVersion() {
            return releasedVersions.getMostRecentRelease().getVersion();
        }

        @Override
        public boolean isLatestGlobalAvailable() {
            return true;
        }

        @Override
        public boolean isLatestNightly() {
            return false;
        }

        @Override
        public String getName() {
            return LATEST_GLOBAL_AVAILABLE;
        }
    }

    private final class AdhocGradleVersionCoverageTestingStrategy extends AbstractGradleVersionCoverageTestingStrategy {
        private final String version;

        private AdhocGradleVersionCoverageTestingStrategy(String version) {
            this.version = version;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getName() {
            return version;
        }
    }
}
