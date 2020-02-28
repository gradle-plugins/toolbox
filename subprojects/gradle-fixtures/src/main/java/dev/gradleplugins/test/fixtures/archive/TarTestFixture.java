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

import dev.gradleplugins.test.fixtures.file.TestFile;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

class TarTestFixture extends ArchiveTestFixture {
    private final TestFile tarFile;

    public TarTestFixture(TestFile tarFile) {
        this(tarFile, null);
    }

    public TarTestFixture(TestFile tarFile, String metadataCharset) {
        this(tarFile, metadataCharset, Charset.defaultCharset().name());
    }

    public TarTestFixture(TestFile tarFile, String metadataCharset, String contentCharset) {
        this.tarFile = tarFile;

        boolean gzip = !tarFile.getName().endsWith("tar");
        try (InputStream inputStream = new FileInputStream(tarFile)) {
            TarInputStream tarInputStream = new TarInputStream(gzip ? new GZIPInputStream(inputStream) : inputStream, metadataCharset);
            for (TarEntry tarEntry = tarInputStream.getNextEntry(); tarEntry != null; tarEntry = tarInputStream.getNextEntry()) {
                addMode(tarEntry.getName(), tarEntry.getMode());
                if (tarEntry.isDirectory()) {
                    continue;
                }
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                tarInputStream.copyEntryContents(stream);
                add(tarEntry.getName(), new String(stream.toByteArray(), contentCharset));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
