package dev.gradleplugins.internal;

import dev.gradleplugins.CompositeGradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.provider.Provider;
import org.gradle.util.VersionNumber;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.internal.GradlePluginTestingStrategyInternal.*;

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
        return new DefaultGradleVersionCoverageTestingStrategy(MINIMUM_GRADLE, () -> {
            val result = minimumVersion.get();
            assertKnownMinimumVersion(result);
            return result;
        });
    }

    @Override
    public GradleVersionCoverageTestingStrategy getCoverageForLatestNightlyVersion() {
        return new DefaultGradleVersionCoverageTestingStrategy(LATEST_NIGHTLY, () -> releasedVersions.getMostRecentSnapshot().getVersion());
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
        return new DefaultGradleVersionCoverageTestingStrategy(LATEST_GLOBAL_AVAILABLE, () -> releasedVersions.getMostRecentRelease().getVersion());
    }

    @Override
    public GradleVersionCoverageTestingStrategy coverageForGradleVersion(String version) {
        if (!isKnownVersion(version)) {
            throw new IllegalArgumentException(String.format("Unknown Gradle version '%s' for adhoc testing strategy.", version));
        }
        return new DefaultGradleVersionCoverageTestingStrategy(version, () -> version);
    }

    @Override
    public CompositeGradlePluginTestingStrategy composite(GradlePluginTestingStrategy firstStrategy, GradlePluginTestingStrategy secondStrategy, GradlePluginTestingStrategy... otherStrategies) {
        List<GradlePluginTestingStrategy> strategies = Stream.concat(Stream.of(firstStrategy, secondStrategy), Arrays.stream(otherStrategies))
                .peek(Objects::requireNonNull)
                .peek(assertNoCompositeStrategies())
                .peek(assertNoDuplicatedStrategies())
                .peek(assertNoDuplicatedStrategyTypes())
                .collect(Collectors.toList());
        return new DefaultCompositeGradlePluginTestingStrategy(strategies);
    }

    private static Consumer<GradlePluginTestingStrategy> assertNoCompositeStrategies() {
        return strategy -> {
            if (strategy instanceof CompositeGradlePluginTestingStrategy) {
                throw new IllegalArgumentException("Unable to compose testing strategy from composite testing strategies.");
            }
        };
    }

    private static Consumer<GradlePluginTestingStrategy> assertNoDuplicatedStrategies() {
        Set<GradlePluginTestingStrategy> strategyHashes = new HashSet<>();
        return strategy -> {
            if (!strategyHashes.add(strategy)) {
                throw new IllegalArgumentException(String.format("Unable to compose testing strategy with multiple %s instances.", strategy.toString()));
            }
        };
    }

    private static Consumer<GradlePluginTestingStrategy> assertNoDuplicatedStrategyTypes() {
        Set<Class<?>> strategyTypes = new HashSet<>();
        return strategy -> {
            if (!strategyTypes.add(strategy.getClass())) {
                throw new IllegalArgumentException(String.format("Unable to compose testing strategy with multiple %s type.", strategy.getClass().getSimpleName()));
            }
        };
    }

    private boolean isKnownVersion(String version) {
        return releasedVersions.getAllVersions().stream().anyMatch(it -> it.getVersion().equals(version));
    }

    private void assertKnownMinimumVersion(String version) {
        if (!isKnownVersion(version)) {
            throw new IllegalArgumentException(String.format("Unknown minimum Gradle version '%s' for testing strategy.", version));
        }
    }

    private final class DefaultGradleVersionCoverageTestingStrategy implements GradlePluginTestingStrategyInternal, GradleVersionCoverageTestingStrategy {
        private final String name;
        private final Supplier<String> versionSupplier;

        DefaultGradleVersionCoverageTestingStrategy(String name, Supplier<String> versionSupplier) {
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
            } else if (!(o instanceof DefaultGradleVersionCoverageTestingStrategy)) {
                return false;
            }
            DefaultGradleVersionCoverageTestingStrategy that = (DefaultGradleVersionCoverageTestingStrategy) o;
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

    @EqualsAndHashCode
    private static final class DefaultCompositeGradlePluginTestingStrategy implements CompositeGradlePluginTestingStrategy {
        private final Iterable<GradlePluginTestingStrategy> strategies;
        @EqualsAndHashCode.Exclude private final String name;

        private DefaultCompositeGradlePluginTestingStrategy(Iterable<GradlePluginTestingStrategy> strategies) {
            this.strategies = strategies;
            this.name = StringUtils.uncapitalize(StreamSupport.stream(strategies.spliterator(), false).map(it -> {
                return StringUtils.capitalize(it.getName());
            }).collect(Collectors.joining()));
        }

        @Override
        public Iterator<GradlePluginTestingStrategy> iterator() {
            return strategies.iterator();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "strategy composed of <" + StreamSupport.stream(strategies.spliterator(), false).map(Object::toString).collect(Collectors.joining(", ")) + ">";
        }
    }
}
