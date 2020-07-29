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

package dev.gradleplugins.test.fixtures.sources;

import lombok.Value;
import lombok.val;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;

@Value
public class SourceFile {
    String path;
    String name;
    String content;

    public File writeToDirectory(File base) {
        return writeToDirectory(base, name);
    }

    private File writeToDirectory(File base, String name) {
        val file = new File(base, name);
        writeToFile(file);
        return file;
    }

    public void writeToFile(File file) {
        try {
            FileUtils.touch(file);
            FileUtils.write(file, content, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new UncheckedIOException(String.format("Unable to create source file at '%s'.", file.getAbsolutePath()), ex);
        }
    }

    public String withPath(String basePath) {
        return String.join("/", basePath, path, name);
    }

    @Override
    public String toString() {
        return "SourceFile{" +
                "path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", content='" + firstContentLine(content) + '\'' +
                '}';
    }

    private static String firstContentLine(String content) {
        String[] tokens = content.split("\n", -1);
        return Arrays.stream(tokens).map(String::trim).filter(line -> !line.isEmpty()).findFirst().map(it -> it + "...").orElse("");
    }
}
