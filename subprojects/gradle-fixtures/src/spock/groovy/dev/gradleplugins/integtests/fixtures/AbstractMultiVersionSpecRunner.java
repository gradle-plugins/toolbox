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

import lombok.Value;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class AbstractMultiVersionSpecRunner<T extends AbstractMultiVersionSpecRunner.VersionedTool> extends AbstractMultiTestRunner {
    public static final CoverageContext UNKNOWN = new CoverageContext(null);
    private final Class<?> target;
    private final Set<CoverageContext> coverageContexts;
    private final IgnoreVersionIf ignoreIf;

    protected abstract Collection<T> getAllVersions();

    protected abstract boolean isAvailable(T version);
    
    protected abstract Collection<Execution> createExecutionsFor(T versionedTool);

    public AbstractMultiVersionSpecRunner(Class<?> target, Set<CoverageContext> coverageContexts) {
        super(target);
        this.target = target;
        this.coverageContexts = coverageContexts;
        this.ignoreIf = target.getAnnotation(IgnoreVersionIf.class);
    }

    protected abstract String getVersions();

    @Override
    protected void createExecutions() {
        String versions = getVersions();
        CoverageContext coverageContext = from(versions);
        if (coverageContext == UNKNOWN) {
            List<String> selectionCriteria = new ArrayList<>(Arrays.asList(versions.split(",")));
            createSelectedExecutions(selectionCriteria);
        } else {
            createExecutionsForContext(coverageContext);
        }
    }

    protected abstract Collection<T> versionUnderTestForContext(CoverageContext coverageContext);

    protected void createExecutionsForContext(CoverageContext coverageContext) {
        if (coverageContext == UNKNOWN) {
            throw new IllegalArgumentException();
        }

        Collection<T> versionsUnderTest = versionUnderTestForContext(coverageContext);

        createNonIgnoredExecutions(versionsUnderTest);
    }

    private void createSelectedExecutions(List<String> selectionCriteria) {
        Collection<T> possibleVersions = getAllVersions();
        Set<T> versionsUnderTest = new HashSet<>();

        for (String criteria : selectionCriteria) {
            CoverageContext candidateCoverageContext = from(criteria);
            if (candidateCoverageContext != UNKNOWN) {
                versionsUnderTest.addAll(versionUnderTestForContext(candidateCoverageContext));
            } else {
                for (T version : possibleVersions) {
                    if (isAvailable(version) && version.matches(criteria)) {
                        versionsUnderTest.add(version);
                    }
                }
            }
        }
        
        createNonIgnoredExecutions(versionsUnderTest);
    }

    private void createNonIgnoredExecutions(Collection<T> versionsUnderTest) {
        for (T version : versionsUnderTest) {
            try {
                boolean shouldIgnore = (Boolean) ignoreIf.value().getConstructor(Class.class, Class.class).newInstance(target, target).call();
                if (shouldIgnore) {
                    continue;
                }
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            for (Execution execution : createExecutionsFor(version)) {
                add(execution);
            }
        }
    }

    @Value
    protected static class CoverageContext {
        String selector;

        CoverageContext(String selector) {
            this.selector = selector;
        }
    }

    private CoverageContext from(String requested) {
        for (CoverageContext context : coverageContexts) {
            if (context.selector.equals(requested)) {
                return context;
            }
        }
        return UNKNOWN;
    }

    public interface VersionedTool {
        boolean matches(String criteria);
    }
}
