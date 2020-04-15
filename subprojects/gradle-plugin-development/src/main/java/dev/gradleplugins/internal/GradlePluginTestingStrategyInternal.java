package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;

import java.util.*;

@AllArgsConstructor
public class GradlePluginTestingStrategyInternal implements GradlePluginTestingStrategy {
    @Getter final Set<String> versionCoverage;

    public static class NightlyAwareGradlePluginTestingStrategyBuilderInternal extends GradlePluginTestingStrategyInternal implements GradlePluginTestingStrategyFactory.NightlyAwareGradlePluginTestingStrategyBuilder {
        public NightlyAwareGradlePluginTestingStrategyBuilderInternal(Set<String> versionCoverage) {
            super(versionCoverage);
        }

        public GradlePluginTestingStrategy includeLatestNightlyVersion() {
            Set<String> newVersionCoverage = new LinkedHashSet<>();
            newVersionCoverage.addAll(getVersionCoverage());
            newVersionCoverage.add("latestNightly");
            return new GradlePluginTestingStrategyInternal(newVersionCoverage);
        }
    }
}
