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

import dev.gradleplugins.test.fixtures.file.ClassFile;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.gradle.api.JavaVersion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JarTestFixture extends ZipTestFixture {
    public final int classFileDescriptor = 0xCAFEBABE;

    public File file;

    public JarTestFixture(File file) {
        this(file, "UTF-8");
    }

    public JarTestFixture(File file, String metadataCharset) {
        this(file, "UTF-8", Charset.defaultCharset().name());
    }

    /**
     * Asserts that the Jar file is well-formed
     */
     public JarTestFixture(File file, String metadataCharset, String contentCharset) {
         super(file, metadataCharset, contentCharset);
         this.file = file;
         isManifestPresentAndFirstEntry();
     }

    /**
     * Asserts that the given service is defined in this jar file.
     */
    public JarTestFixture hasService(String serviceName, String serviceImpl) {
        assertFilePresent("META-INF/services/" + serviceName, serviceImpl);
        return this;
    }

    /**
     * Asserts that the manifest file is present and first entry in this jar file.
     */
    public void isManifestPresentAndFirstEntry() {
        try (ZipFile zipFile = new ZipFile(file, metadataCharset)) {
            Enumeration<ZipEntry> entries = zipFile.getEntries();
            ZipEntry zipEntry = entries.nextElement();
            if(zipEntry.getName().equalsIgnoreCase("META-INF/")) {
                zipEntry = entries.nextElement();
            }
            String firstEntryName = zipEntry.getName();
            assert firstEntryName.equalsIgnoreCase(JarFile.MANIFEST_NAME);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public JarTestFixture hasDescendants(String... relativePaths) {
        List<String> allDescendants = new ArrayList<>(Arrays.asList(relativePaths));
        allDescendants.add(JarFile.MANIFEST_NAME);
        super.hasDescendants(allDescendants);
        return this;
    }

    public JavaVersion getJavaVersion() {
        try (JarFile jarFile = new JarFile(file)) {
            //take the first class file
            JarEntry classEntry = Collections.list(jarFile.entries()).stream().filter(entry -> entry.getName().endsWith(".class")).findFirst().orElseThrow(() -> new RuntimeException("Could not find a class entry for: " + file));
            ClassFile classFile = new ClassFile(jarFile.getInputStream(classEntry));
            return classFile.getJavaVersion();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Manifest getManifest() {
        try {
            return new Manifest(new ByteArrayInputStream(content("META-INF/MANIFEST.MF").getBytes(contentCharset)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
