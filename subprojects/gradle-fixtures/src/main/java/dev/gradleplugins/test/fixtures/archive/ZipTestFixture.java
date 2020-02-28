/*
 * Copyright 2013 the original author or authors.
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

package dev.gradleplugins.test.fixtures.archive;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import dev.gradleplugins.test.fixtures.file.TestFile;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;

public class ZipTestFixture extends ArchiveTestFixture {
    protected final String metadataCharset;
    protected final String contentCharset;

    public ZipTestFixture(File file) {
        this(file, Charset.defaultCharset().name());
    }

    public ZipTestFixture(File file, String metadataCharset) {
        this(file, metadataCharset, Charset.defaultCharset().name());
    }

    public ZipTestFixture(File file, String metadataCharset, String contentCharset) {
        new TestFile(file).assertIsFile();
        this.metadataCharset = metadataCharset;
        this.contentCharset = contentCharset;
        try (ZipFile zipFile = new ZipFile(file, this.metadataCharset)) {
            Enumeration<ZipEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String content = getContentForEntry(entry, zipFile);
                if (!entry.isDirectory()) {
                    add(entry.getName(), content);
                }
                addMode(entry.getName(), entry.getUnixMode());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getContentForEntry(ZipEntry entry, ZipFile zipFile) {
        String extension = Files.getFileExtension(entry.getName());
        if (!(Arrays.asList("jar", "zip").contains(extension))) {
            try {
                return new String(ByteStreams.toByteArray(zipFile.getInputStream(entry)), contentCharset);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return "";
    }
}
