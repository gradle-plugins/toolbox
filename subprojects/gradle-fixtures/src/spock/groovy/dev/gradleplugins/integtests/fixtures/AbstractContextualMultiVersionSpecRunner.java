/*
 * Copyright 2018 the original author or authors.
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
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractContextualMultiVersionSpecRunner<T extends AbstractMultiVersionSpecRunner.VersionedTool> extends AbstractMultiVersionSpecRunner<T> {
    public static final String VERSIONS_SYSPROP_NAME = "org.gradle.integtest.versions"; // TODO: Change property name
    public static final CoverageContext DEFAULT = new CoverageContext("default");
    public static final CoverageContext LATEST = new CoverageContext("latest");
    public static final CoverageContext PARTIAL = new CoverageContext("partial");
    public static final CoverageContext FULL = new CoverageContext("all");

    @Override
    protected String getVersions() {
        return System.getProperty(VERSIONS_SYSPROP_NAME, DEFAULT.getSelector());
    }

    protected abstract Collection<T> getAllVersions();

    protected Collection<T> getLatestVersion() {
        return Collections.singleton(Iterables.getLast(getAllVersions()));
    }

    protected Collection<T> getQuickVersions() {
        for (T next : getAllVersions()) {
            if (isAvailable(next)) {
                return Collections.singleton(next);
            }
        }
        return Collections.emptyList();
    }

    protected Collection<T> getPartialVersions() {
        Collection<T> allVersions = getAllVersions();
        Set<T> partialVersions = new HashSet<>();
        T firstAvailable = getFirstAvailable(allVersions);
        if (firstAvailable != null) {
            partialVersions.add(firstAvailable);
        }
        T lastAvailable = getLastAvailable(allVersions);
        if (lastAvailable != null) {
            partialVersions.add(lastAvailable);
        }
        return partialVersions;
    }

    private Collection<T> getAvailableVersions() {
        Set<T> allAvailable = getAllVersions().stream().filter(this::isAvailable).collect(Collectors.toSet());
        return allAvailable;
    }

    private T getFirstAvailable(Collection<T> versions) {
        for (T next : versions) {
            if (isAvailable(next)) {
                return next;
            }
        }
        return null;
    }

    private T getLastAvailable(Collection<T> versions) {
        T lastAvailable = null;

        for (T next : versions) {
            if (isAvailable(next)) {
                lastAvailable = next;
            }
        }

        return lastAvailable;
    }

    public AbstractContextualMultiVersionSpecRunner(Class<?> target) {
        super(target, ImmutableSet.of(DEFAULT, LATEST, PARTIAL, FULL));
    }

    protected Collection<T> versionUnderTestForContext(CoverageContext coverageContext) {
        if (coverageContext == DEFAULT) {
            return getQuickVersions();
        } else if (coverageContext == LATEST) {
            return getLatestVersion();
        } else if (coverageContext == PARTIAL) {
            return getPartialVersions();
        } else if (coverageContext == FULL) {
            return getAvailableVersions();
        }
        throw new IllegalArgumentException();
    }
}
