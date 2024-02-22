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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class SourceKind {
    public static final SourceKind C = new SourceKind("c", "c");
    public static final SourceKind CPP = new SourceKind("cpp", "cpp", "cc", "cxx");
    public static final SourceKind OBJECTIVE_C = new SourceKind("objective-c", "m");
    public static final SourceKind OBJECTIVE_CPP = new SourceKind("objective-cpp", "mm");
    public static final SourceKind SWIFT = new SourceKind("swift", "swift");
    public static final SourceKind JAVA = new SourceKind("java", "java");
    public static final SourceKind GROOVY = new SourceKind("groovy", "groovy");
    public static final SourceKind KOTLIN = new SourceKind("kotlin", "kt");
    public static final SourceKind HEADER = new SourceKind("header", "h", "hpp");
    public static final SourceKind UNKNOWN = new SourceKind("unknown");
    private static final Set<SourceKind> DEFAULT_KINDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(C, CPP, OBJECTIVE_C, OBJECTIVE_CPP, SWIFT, JAVA, GROOVY, KOTLIN, HEADER)));

    private final String identifier;
    private final Set<String> fileExtensions; // excluded from equals/hashCode

    private SourceKind(String identifier, String... fileExtensions) {
        this.identifier = identifier;
        this.fileExtensions = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(fileExtensions)));
    }

    public static SourceKind of(String identifier) {
        return new SourceKind(identifier);
    }

    public static SourceKind valueOf(String fileName) {
        final String extension = getExtension(fileName);
        return DEFAULT_KINDS.stream().filter(it -> it.fileExtensions.contains(extension)).findFirst().orElse(UNKNOWN);
    }

    //region FilenameUtils#getExtension
    private static final int NOT_FOUND = -1;
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static int indexOfExtension(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int extensionPos = filename.lastIndexOf('.');
        final int lastSeparator = indexOfLastSeparator(filename);
        return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
    }

    private static int indexOfLastSeparator(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    private static String getExtension(final String filename) {
        if (filename == null) {
            return null;
        }
        final int index = indexOfExtension(filename);
        if (index == NOT_FOUND) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }
    //endregion

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SourceKind that = (SourceKind) o;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
