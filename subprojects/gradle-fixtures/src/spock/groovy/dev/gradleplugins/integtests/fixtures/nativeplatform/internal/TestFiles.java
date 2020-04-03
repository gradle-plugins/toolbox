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
package dev.gradleplugins.integtests.fixtures.nativeplatform.internal;

import org.gradle.api.internal.file.DefaultFileLookup;
import org.gradle.api.internal.file.FileLookup;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.process.internal.DefaultExecActionFactory;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.process.internal.ExecFactory;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;

public class TestFiles {
    private static final ServiceRegistry SERVICE_REGISTRY = ((ProjectInternal)ProjectBuilder.builder().build()).getServices();
    private static final DefaultFileLookup FILE_LOOKUP = (DefaultFileLookup)SERVICE_REGISTRY.get(FileLookup.class);
    private static final DefaultExecActionFactory EXEC_FACTORY = (DefaultExecActionFactory)SERVICE_REGISTRY.get(ExecActionFactory.class);

    /**
     * Returns a resolver with the given base directory.
     */
    public static FileResolver resolver(File baseDir) {
        return FILE_LOOKUP.getFileResolver(baseDir);
    }

    public static ExecFactory execFactory() {
        return EXEC_FACTORY;
    }

    public static ExecActionFactory execActionFactory() {
        return execFactory();
    }
}
