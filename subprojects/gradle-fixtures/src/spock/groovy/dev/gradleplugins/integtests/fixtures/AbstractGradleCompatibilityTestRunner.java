/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.integtests.fixtures;

import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.versions.ReleasedVersionDistributions;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A base class for those test runners which execute a test multiple times against a set of Gradle versions.
 */
public abstract class AbstractGradleCompatibilityTestRunner extends AbstractMultiVersionSpecRunner<GradleDistributionTool> {
    final ReleasedVersionDistributions releasedVersions = new ReleasedVersionDistributions();

    public AbstractGradleCompatibilityTestRunner(Class<?> target, Set<CoverageContext> coverageContexts) {
        super(target, coverageContexts);
    }

    @Override
    protected Collection<GradleDistributionTool> getAllVersions() {
        List<GradleDistribution> allSupportedVersions = chooseVersionsToTest(releasedVersions);
        List<GradleDistribution> sortedDistributions = allSupportedVersions.stream().sorted(Comparator.comparing(GradleDistribution::getVersion)).collect(Collectors.toList());
        return sortedDistributions.stream().map(this::versionedToolFrom).collect(Collectors.toList());
    }

    @Override
    protected boolean isAvailable(GradleDistributionTool version) {
        return true;
    }

    @Override
    protected Collection<Execution> createExecutionsFor(GradleDistributionTool versionedTool) {
        if (versionedTool.getIgnored() != null) {
            return Collections.singleton(new IgnoredVersion(versionedTool.getDistribution(), versionedTool.getIgnored()));
        } else {
            return createDistributionExecutionsFor(versionedTool);
        }
    }

    protected GradleDistributionTool versionedToolFrom(GradleDistribution distribution) {
        if (!distribution.worksWith(Jvm.current())) {
            return new GradleDistributionTool(distribution, "does not work with current JVM");
        } else if (!distribution.worksWith(OperatingSystem.current())) {
            return new GradleDistributionTool(distribution, "does not work with current OS");
        } else {
            return new GradleDistributionTool(distribution);
        }
    }

    protected List<GradleDistribution> chooseVersionsToTest(ReleasedVersionDistributions releasedVersions) {
        return Collections.emptyList();
    }

    protected Collection<Execution> createDistributionExecutionsFor(GradleDistributionTool versionedTool) {
        return Collections.emptyList();
    }

    private static class IgnoredVersion extends Execution {
        private final GradleDistribution distribution;
        private final String why;

        private IgnoredVersion(GradleDistribution distribution, String why) {
            this.distribution = distribution;
            this.why = why;
        }

        @Override
        protected boolean isTestEnabled(TestDetails testDetails) {
            return false;
        }

        @Override
        protected String getDisplayName() {
            return String.format("%s %s", distribution.getVersion(), why);
        }
    }
}
