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

package dev.gradleplugins.integtests.fixtures.nativeplatform;

import dev.gradleplugins.test.fixtures.file.ExecOutput;
import dev.gradleplugins.test.fixtures.file.TestFile;
import org.apache.commons.io.FilenameUtils;
import org.gradle.internal.os.OperatingSystem;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NativeInstallationFixture {
    private final TestFile installDirectory;
    private final OperatingSystem os;

	public NativeInstallationFixture(TestFile installDirectory, OperatingSystem os) {
        this.installDirectory = installDirectory;
        this.os = os;
    }

    public ExecOutput exec(Object... args) {
        assertInstalled();
        return scriptFile().get().exec(args);
    }

    private Optional<TestFile> scriptFile() {
	    File[] files = installDirectory.listFiles(File::isFile);
	    checkNotNull(files, "Couldn't list files inside '%s'", installDirectory.getAbsolutePath());

        Stream<TestFile> fileStream = Arrays.stream(files).map(TestFile::of);
        if (os.isWindows()) {
            fileStream = fileStream.filter(it -> FilenameUtils.isExtension(it.getName(), "bat"));
        }

        return fileStream.findFirst();
    }

    public NativeInstallationFixture assertInstalled() {
        installDirectory.assertIsDirectory();
        final Optional<TestFile> script = scriptFile();
        assert script.isPresent();

        TestFile libDir = installDirectory.file("lib");
        libDir.assertIsDirectory();
        libDir.file(os.getExecutableName(script.get().getName())).assertIsFile();
        return this;
    }

    public NativeInstallationFixture assertNotInstalled() {
        installDirectory.assertDoesNotExist();
        return this;
    }

    public NativeInstallationFixture assertIncludesLibraries(String... names) {
        Set<String> expected = Arrays.asList(names).stream().map(os::getSharedLibraryName).collect(Collectors.toSet());
        assertThat(getLibraryFiles().stream().map(File::getName).collect(Collectors.toSet()), equalTo(expected));
        return this;
    }

    private List<File> getLibraryFiles() {
        installDirectory.assertIsDirectory();
        TestFile libDir = installDirectory.file("lib");
        libDir.assertIsDirectory();
        List<File> libFiles;
        if (os.isWindows()) {
            libFiles = Arrays.stream(libDir.listFiles()).filter(it -> it.isFile() && !it.getName().endsWith(".exe")).collect(Collectors.toList());
        } else {
            libFiles = Arrays.stream(libDir.listFiles()).filter(it -> it.isFile() && !it.getName().contains(".")).collect(Collectors.toList());
        }
        return libFiles;
    }
}
