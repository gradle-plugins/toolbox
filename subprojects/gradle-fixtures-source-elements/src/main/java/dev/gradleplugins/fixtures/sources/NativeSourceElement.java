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
package dev.gradleplugins.fixtures.sources;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NativeSourceElement extends SourceElement {
    public SourceElement getHeaders() {
        return empty();
    }

    public abstract SourceElement getSources();

    public List<SourceFile> getFiles() {
        List<SourceFile> files = new ArrayList<>();
        files.addAll(getSources().getFiles());
        files.addAll(getHeaders().getFiles());
        return files;
    }

    public static SourceElement ofHeaders(SourceElement element) {
        return ofFiles(element.getFiles().stream().filter(it -> it.getKind().equals(SourceKind.HEADER)).collect(Collectors.toList()));
    }

    public static SourceElement ofSources(SourceElement element) {
        return ofFiles(element.getFiles().stream().filter(it -> !it.getKind().equals(SourceKind.HEADER)).collect(Collectors.toList()));
    }


    public List<String> getSourceFileNamesWithoutHeaders() {
        return getSourceFileNames().stream().filter(sourceFileName -> !sourceFileName.endsWith(".h")).collect(Collectors.toList());
    }

    public static NativeSourceElement ofNativeElements(final NativeSourceElement... elements) {
        return new NativeSourceElement() {
            @Override
            public SourceElement getHeaders() {
                return ofElements(Arrays.stream(elements).map(NativeSourceElement::getHeaders).collect(Collectors.toList()));
            }

            @Override
            public SourceElement getSources() {
                return ofElements(Arrays.stream(elements).map(NativeSourceElement::getSources).collect(Collectors.toList()));
            }

            @Override
            public List<SourceFile> getFiles() {
                List<SourceFile> files = new ArrayList<SourceFile>();
                for (SourceElement element : elements) {
                    files.addAll(element.getFiles());
                }
                return files;
            }

            @Override
            public void writeToProject(File projectDir) {
                for (SourceElement element : elements) {
                    element.writeToProject(projectDir);
                }
            }
        };
    }
}
