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

import com.google.common.collect.ImmutableSet;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.versions.ReleasedVersionDistributions;
import groovy.lang.Closure;
import org.gradle.util.GUtil;
import org.gradle.util.GradleVersion;
import org.gradle.util.VersionNumber;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A base class for those test runners which execute a test multiple times against a set of Gradle versions.
 */
public class GradleCompatibilityTestRunner extends AbstractGradleCompatibilityTestRunner {
    public static final String VERSIONS_SYSPROP_NAME = "dev.gradleplugins.gradleVersions";
    public static final String MINIMUM_VERSION_SYSPROP_NAME = "dev.gradleplugins.minimumGradleVersion";
    public static final CoverageContext DEFAULT = new CoverageContext("default");
    public static final CoverageContext LATEST_NIGHTLY = new CoverageContext("latestNightly");
    public static final CoverageContext LATEST_MINOR = new CoverageContext("latestMinor");
    public static final CoverageContext MINIMUM = new CoverageContext("minimum");
    public static final CoverageContext FULL = new CoverageContext("all");
    private final Class<? extends AbstractGradleSpecification> target;

    public GradleCompatibilityTestRunner(Class<? extends AbstractGradleSpecification> target) {
        super(target, ImmutableSet.of(DEFAULT, LATEST_NIGHTLY, LATEST_MINOR, MINIMUM, FULL));
        this.target = target;
    }

    @Override
    protected List<GradleDistribution> chooseVersionsToTest(ReleasedVersionDistributions releasedVersions) {
        // TODO: Fails nicely if property doesn't exists
        GradleVersion minimumGradleVersion = GradleVersion.version(System.getProperty(MINIMUM_VERSION_SYSPROP_NAME, null));
        return releasedVersions.getAll().stream().filter(it -> it.getVersion().compareTo(minimumGradleVersion) >= 0).sorted(Comparator.comparing(GradleDistribution::getVersion)).collect(Collectors.toList());
    }

    @Override
    protected String getVersions() {
        return System.getProperty(VERSIONS_SYSPROP_NAME, DEFAULT.getSelector());
    }

    @Override
    protected Collection<GradleDistributionTool> versionUnderTestForContext(CoverageContext coverageContext) {
        if (coverageContext == DEFAULT) {
            // TODO: It should really just default to what TestKit would normally use
            return Collections.singleton(getMinimumVersion());
        } else if (coverageContext == LATEST_NIGHTLY) {
            return Collections.singleton(versionedToolFrom(releasedVersions.getMostRecentSnapshot()));
        } else if (coverageContext == LATEST_MINOR) {
            return getLatestMinorVersions();
        } else if (coverageContext == MINIMUM) {
            return Collections.singleton(getMinimumVersion());
        } else if (coverageContext == FULL) {
            return getAllVersions();
        }
        throw new IllegalArgumentException();
    }

    private Collection<GradleDistributionTool> getLatestMinorVersions() {
        Map<String, List<GradleDistributionTool>> groupedGradleReleases = getAllVersions().stream().collect(Collectors.groupingBy(GradleCompatibilityTestRunner::toVersionGroup, Collectors.toList()));
        return groupedGradleReleases.values().stream().map(it -> it.stream().max(Comparator.comparing(dist -> dist.getDistribution().getVersion())).get()).collect(Collectors.toList());
    }

    private GradleDistributionTool getMinimumVersion() {
        // TODO: Fail nicely if property doesn't exists
        String minimumGradleVersion = System.getProperty(MINIMUM_VERSION_SYSPROP_NAME, null);
        return getAllVersions().stream().filter(it -> it.matches(minimumGradleVersion)).findFirst().get();
    }

    @Override
    protected boolean isAvailable(GradleDistributionTool version) {
        return true;
    }












    // TODO: Maybe this should be higher up in class hiarchy
    @Override
    protected Collection<Execution> createDistributionExecutionsFor(GradleDistributionTool versionedTool) {
        GradleDistribution distribution = versionedTool.getDistribution();
        return Collections.singleton(new GradleVersionExecution(distribution, isEnabled(distribution)));
    }

    protected boolean isEnabled(GradleDistribution previousVersion) {
        Closure<Object> ignoreVersions = getAnnotationClosure(target, IgnoreVersions.class, new Closure<Object>(null) {
            @Override
            public Object call(Object... args) {
                return null;
            }
        });
        if (GUtil.isTrue(ignoreVersions.call(previousVersion))) {
            return false;
        }

        TargetVersions versionsAnnotation = target.getAnnotation(TargetVersions.class);
        if (versionsAnnotation == null) {
            return true;
        }

        String[] targetGradleVersions = versionsAnnotation.value();
        for (String targetGradleVersion : targetGradleVersions) {
            if (isMatching(targetGradleVersion, previousVersion.getVersion().getVersion())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMatching(String targetGradleVersion, String candidate) {
        if (targetGradleVersion.endsWith("+")) {
            String minVersion = targetGradleVersion.substring(0, targetGradleVersion.length() - 1);
            return GradleVersion.version(minVersion).compareTo(GradleVersion.version(candidate)) <= 0;
        }
        return targetGradleVersion.equals(candidate);
    }

    private static Closure<Object> getAnnotationClosure(Class<?> target, Class<IgnoreVersions> annotation, Closure<Object> defaultValue) {
        IgnoreVersions a = target.getAnnotation(annotation);
        try {
            return a != null ? a.value().getConstructor(Class.class, Class.class).newInstance(target, target) : defaultValue;
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class GradleVersionExecution extends AbstractMultiTestRunner.Execution {
        final GradleDistribution previousVersion;
        final boolean enabled;

        GradleVersionExecution(GradleDistribution previousVersion, boolean enabled) {
            this.previousVersion = previousVersion;
            this.enabled = enabled;
        }

        @Override
        protected String getDisplayName() {
            return previousVersion.getVersion().getVersion();
        }

        @Override
        protected void before() {
            AbstractGradleSpecification.useGradleDistribution(previousVersion);
        }

        @Override
        protected boolean isTestEnabled(AbstractMultiTestRunner.TestDetails testDetails) {
            return enabled;
        }
    }









    private static String toVersionGroup(GradleDistributionTool it) {
        VersionNumber version = VersionNumber.parse(it.getDistribution().getVersion().getVersion());
        return String.valueOf(version.getMajor());
    }
}
