/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.util.Collections;
import java.util.List;

@SourceFileLocation(file = "groovy-gradle-plugin/src/groovy/com/example/BasicPluginFunctionalTest.groovy")
public final class BasicGradlePluginTestKitTest extends RegularFileContent {
    private final String sourceSetName;

    public BasicGradlePluginTestKitTest() {
        this("test");
    }

    public BasicGradlePluginTestKitTest(String sourceSetName) {
        this.sourceSetName = sourceSetName;
    }

    @Override
    protected String getPath() {
        return "groovy/com/example";
    }

    @Override
    public String getSourceSetName() {
        return sourceSetName;
    }
}
