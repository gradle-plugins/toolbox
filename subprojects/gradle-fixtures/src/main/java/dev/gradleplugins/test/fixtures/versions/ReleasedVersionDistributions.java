/*
 * Copyright 2012 the original author or authors.
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

package dev.gradleplugins.test.fixtures.versions;

import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Transformer;
import org.gradle.api.specs.Spec;
import org.gradle.internal.impldep.com.google.gson.Gson;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GradleVersion;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.gradle.util.CollectionUtils.sort;

/**
 * Provides access to {@link GradleDistribution}s for versions of Gradle that have been released.
 *
 * Only versions that are suitable for testing against are made available.
 */
public class ReleasedVersionDistributions {

    private Properties properties;
    private List<GradleDistribution> distributions;

//    private Properties getProperties() {
//        if (properties == null) {
//            properties = versionsFactory.create();
//        }
//
//        return properties;
//    }

    public GradleDistribution getMostRecentRelease() {
        try (Reader reader = new InputStreamReader(new URL("https://services.gradle.org/versions/current").openConnection().getInputStream())) {
            GradleRelease gradleRelease = new Gson().fromJson(reader, GradleRelease.class);
            return GradleDistributionFactory.distribution(gradleRelease.version);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get the last version", e);
        }
    }

    public GradleDistribution getMostRecentReleaseSnapshot() {
        try (Reader reader = new InputStreamReader(new URL("https://services.gradle.org/versions/release-nightly").openConnection().getInputStream())) {
            GradleRelease gradleRelease = new Gson().fromJson(reader, GradleRelease.class);
            return GradleDistributionFactory.distribution(gradleRelease.version);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get the last snapshot version", e);
        }
    }

    public GradleDistribution getMostRecentSnapshot() {
        try (Reader reader = new InputStreamReader(new URL("https://services.gradle.org/versions/nightly").openConnection().getInputStream())) {
            GradleRelease gradleRelease = new Gson().fromJson(reader, GradleRelease.class);
            return GradleDistributionFactory.distribution(gradleRelease.version);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get the last snapshot version", e);
        }
    }

    public List<GradleDistribution> getAll() {
        if (distributions == null) {
            try (Reader reader = new InputStreamReader(new URL("https://services.gradle.org/versions/all").openConnection().getInputStream())) {
                List<GradleRelease> gradleReleases = Arrays.asList(new Gson().fromJson(reader, GradleRelease[].class));
                gradleReleases = gradleReleases.stream().filter(it -> !it.snapshot && it.rcFor.isEmpty()).collect(Collectors.toList());
                distributions = gradleReleases.stream().map(it -> GradleDistributionFactory.distribution(it.version)).collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        return distributions;
    }

    private static class GradleRelease {
        String version;
        boolean snapshot;
        boolean current;
        String rcFor;
    }

    public List<GradleDistribution> getSupported() {
        final GradleVersion firstSupported = GradleVersion.version("1.0");
        return getAll().stream().filter(element -> element.getVersion().compareTo(firstSupported) >= 0).collect(Collectors.toList());
    }

    public GradleDistribution getDistribution(final GradleVersion gradleVersion) {
        return getAll().stream().filter(element -> element.getVersion().equals(gradleVersion)).findFirst().get(); // assuming always present
    }

    public GradleDistribution getDistribution(final String gradleVersion) {
        return getAll().stream().filter(element -> element.getVersion().getVersion().equals(gradleVersion)).findFirst().get(); // assuming always present
    }

    public GradleDistribution getPrevious(final GradleVersion gradleVersion) {
        GradleDistribution distribution = getDistribution(gradleVersion);
        List<GradleDistribution> sortedDistributions = distributions.stream().sorted(new Comparator<GradleDistribution>() {
            @Override
            public int compare(GradleDistribution dist1, GradleDistribution dist2) {
                return dist1.getVersion().compareTo(dist2.getVersion());
            }
        }).collect(Collectors.toList());
        int distributionIndex = sortedDistributions.indexOf(distribution) - 1;
        return distributionIndex >= 0 ? sortedDistributions.get(distributionIndex) : null;
    }
}
