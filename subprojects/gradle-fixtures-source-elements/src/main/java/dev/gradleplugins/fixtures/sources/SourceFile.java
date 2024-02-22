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

package dev.gradleplugins.fixtures.sources;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

public final class SourceFile {
    private final String path;
    private final String name;
    private final String content;

    public SourceFile(String path, String name, String content) {
        this.path = path;
        this.name = name;
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public File writeToDirectory(File base) {
        return writeToDirectory(base, name);
    }

    private File writeToDirectory(File base, String name) {
        final File file = new File(base, String.join(File.separator, path, name));
        writeToFile(file);
        return file;
    }

    public void writeToFile(File file) {
        try {
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), content.getBytes(Charset.defaultCharset()));
        } catch (IOException ex) {
            throw new UncheckedIOException(String.format("Unable to create source file at '%s'.", file.getAbsolutePath()), ex);
        }
    }

    public String withPath(String basePath) {
        return String.join("/", basePath, path, name);
    }

    private static String firstContentLine(String content) {
        String[] tokens = content.split("\n", -1);
        return Arrays.stream(tokens).map(String::trim).filter(line -> !line.isEmpty()).findFirst().map(it -> it + "...").orElse("");
    }

    public SourceKind getKind() {
        return SourceKind.valueOf(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SourceFile that = (SourceFile) o;
        return Objects.equals(path, that.path) && Objects.equals(name, that.name) && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, name, content);
    }

    @Override
    public String toString() {
        return "SourceFile{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", content='" + firstContentLine(content) + '\'' +
                '}';
    }
}
