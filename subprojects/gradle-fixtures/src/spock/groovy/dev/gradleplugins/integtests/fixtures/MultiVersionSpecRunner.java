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

package dev.gradleplugins.integtests.fixtures;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs the target test class against the versions specified in a {@link TargetVersions} or {@link TargetCoverage}
 */
public class MultiVersionSpecRunner extends AbstractContextualMultiVersionSpecRunner<DefaultVersionedTool> {
    private final Class<?> target;
    private final TargetVersions versions;
    private final TargetCoverage coverage;

    public MultiVersionSpecRunner(Class<?> target) {
        super(target);
        this.target = target;
        this.versions = target.getAnnotation(TargetVersions.class);
        this.coverage = target.getAnnotation(TargetCoverage.class);
    }

    @Override
    protected Collection<DefaultVersionedTool> getAllVersions() {
        if (versions != null) {
            return versionsFrom(Arrays.asList(versions.value()));
        } else if (coverage != null) {
            try {
                return versionsFrom((List<Object>)coverage.value().getConstructor(Class.class, Class.class).newInstance(target, target).call());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException(String.format("Target class '%s' is not annotated with @%s nor with @%s.", target.getSimpleName(), TargetVersions.class.getSimpleName(), TargetCoverage.class.getSimpleName()));
        }
    }

    @Override
    protected boolean isAvailable(DefaultVersionedTool version) {
        return true;
    }

    @Override
    protected Collection<Execution> createExecutionsFor(DefaultVersionedTool versionedTool) {
        return Collections.singleton(new VersionExecution(versionedTool.getVersion()));
    }

    static List<DefaultVersionedTool> versionsFrom(List<Object> versions) {
        return versions.stream().map(DefaultVersionedTool::new).collect(Collectors.toList());
    }

    private static class VersionExecution extends AbstractMultiTestRunner.Execution {
        private final Object version;

        VersionExecution(Object version) {
            this.version = version;
        }

        @Override
        protected String getDisplayName() {
            return version.toString();
        }

        @Override
        protected void before() {
            try {
                Method m = target.getMethod("setVersion", Object.class);
                m.invoke(target, version);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Please define a setVersion(Object) static setter. You can also just use `static def version`.");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Make sure the setVersion(Object) static setter is public.");
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Error while invoking setVersion(Object) on " + target.getSimpleName());
            }
        }
    }
}
