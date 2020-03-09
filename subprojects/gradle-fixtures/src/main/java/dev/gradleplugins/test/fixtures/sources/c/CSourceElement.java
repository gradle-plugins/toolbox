/*
 * Copyright 2017 the original author or authors.
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

package dev.gradleplugins.test.fixtures.sources.c;

import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CSourceElement extends SourceElement {
    public abstract SourceElement getHeaders();

    public abstract SourceElement getSources();

    @Override
    public List<SourceFile> getFiles() {
        List<SourceFile> files = new ArrayList<>();
        files.addAll(getSources().getFiles());
        files.addAll(getHeaders().getFiles());
        return files;
    }

    public List<String> getSourceFileNamesWithoutHeaders() {
        return getSourceFileNames().stream().filter(sourceFileName -> !sourceFileName.endsWith(".h")).collect(Collectors.toList());
    }
}
