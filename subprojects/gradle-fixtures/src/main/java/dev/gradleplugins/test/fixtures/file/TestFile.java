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

package dev.gradleplugins.test.fixtures.file;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import dev.gradleplugins.test.fixtures.util.RetryUtil;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.util.TeeOutputStream;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.hamcrest.Matchers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class TestFile extends File {
    private boolean useNativeTools = false;

    public TestFile(File file, Object... path) {
        super(join(file, path).getAbsolutePath());
    }

    public TestFile(String path) {
        this(new File(path));
    }

    private TestFile(File file, boolean followLinks) {
        super(file.getAbsolutePath());
    }

    // TODO: Unit test this method
    //   Above all, figure out what the default behavior (aka the canonical of the file)
    public static TestFile of(File file, LinkOption... options) {
        if (asList(options).contains(LinkOption.NOFOLLOW_LINKS)) {
            return new TestFile(file, false);
        }
        return new TestFile(file);
    }

    public TestFile usingNativeTools() {
        useNativeTools = true;
        return this;
    }

    public TestFile assertExists() {
        assertTrue(String.format("%s does not exist", this), exists());
        return this;
    }

    public TestFile assertDoesNotExist() {
        assertFalse(String.format("%s should not exist", this), exists());
        return this;
    }

    /**
     * Asserts that this file contains exactly the given set of descendants.
     */
    // TODO: Unit test this method
    public TestFile assertHasDescendants(String... descendants) {
        Set<String> actual = new TreeSet<String>();
        assertIsDirectory();
        visit(actual, "", this);
        Set<String> expected = new TreeSet<String>(asList(descendants));

        Set<String> extras = new TreeSet<String>(actual);
        extras.removeAll(expected);
        Set<String> missing = new TreeSet<String>(expected);
        missing.removeAll(actual);

        assertEquals(String.format("For dir: %s\n extra files: %s, missing files: %s, expected: %s", this, extras, missing, expected), expected, actual);

        return this;

    }

    private void visit(Set<String> names, String prefix, File file) {
        for (File child : file.listFiles()) {
            if (child.isFile()) {
                names.add(prefix + child.getName());
            } else if (child.isDirectory()) {
                visit(names, prefix + child.getName() + "/", child);
            }
        }
    }

    public TestFile assertIsFile() {
        assertTrue(String.format("%s is not a file", this), isFile());
        return this;
    }

    public TestFile assertIsDirectory() {
        assertTrue(String.format("%s is not a directory", this), isDirectory());
        return this;
    }

    public TestFile assertIsSymbolicLink() {
        assertTrue(String.format("%s is not a symbolic link", this), isSymbolicLink());
        return this;
    }

    public TestFile assertIsEmptyDirectory() {
        assertIsDirectory();
        assertHasDescendants();
        return this;
    }

    public TestFile cleanDirectory() throws IOException {
        assertIsDirectory();
        File[] files = listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    of(file).forceDeleteDirectory();
                } else {
                    file.delete();
                }
            }
        }
        return this;
    }

    /**
     * Recursively delete this directory, reporting all failed paths.
     * @return this instance
     * @throws IOException if unable to delete the directory
     */
    // TODO: Test this method
    public TestFile forceDeleteDir() throws IOException {
        if (isDirectory()) {

            if (Files.isSymbolicLink(toPath())) {
                if (!delete()) {
                    throw new IOException("Unable to delete symlink: " + getCanonicalPath());
                }
            } else {
                List<String> errorPaths = new ArrayList<>();
                Files.walkFileTree(toPath(), new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toFile().delete()) {
                            errorPaths.add(file.toFile().getCanonicalPath());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (!dir.toFile().delete()) {
                            errorPaths.add(dir.toFile().getCanonicalPath());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (!errorPaths.isEmpty()) {
                    StringBuilder builder = new StringBuilder()
                            .append("Unable to recursively delete directory ")
                            .append(getCanonicalPath())
                            .append(", failed paths:\n");
                    for (String errorPath : errorPaths) {
                        builder.append("\t- ").append(errorPath).append("\n");
                    }
                    throw new IOException(builder.toString());
                }
            }
        } else if (exists()) {
            if (!delete()) {
                throw new IOException("Unable to delete file: " + getCanonicalPath());
            }
        }
        return this;
    }

    // TODO: Unit test this method
    //    - path to file
    //    - path to dir
    //    - path to link (this one will give a TestFile pointing to the actual linked file
    //    - path to link (non existing link)
    public TestFile file(Object... path) {
        try {
            return new TestFile(this, path);
        } catch (RuntimeException e) {
            throw new RuntimeException(String.format("Could not locate file '%s' relative to '%s'.", Arrays.toString(path), this), e);
        }
    }

    public TestFile createFile() {
        getParentFile().createDirectory();
        try {
            assertTrue(isFile() || createNewFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public TestFile createFile(Object path) {
        return file(path).createFile();
    }

    public TestFile createDirectory(Object path) {
        return new TestFile(this, path).createDirectory();
    }

    public TestFile createDirectory() {
        if (mkdirs()) {
            return this;
        }
        if (isDirectory()) {
            return this;
        }
        throw new AssertionError("Problems creating dir: " + this
                + ". Diagnostics: exists=" + this.exists() + ", isFile=" + this.isFile() + ", isDirectory=" + this.isDirectory());
    }

    public TestFile createSymbolicLink(File target) {
        getParentFile().createDirectory();
        try {
            Files.createSymbolicLink(toPath(), target.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public TestFile createSymbolicLink(String target) {
        return createSymbolicLink(new File(target));
    }

    private static File join(File file, Object[] path) {
        File current = file.getAbsoluteFile();
        for (Object p : path) {
            current = new File(current, p.toString());
        }
        try {
            return current.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not canonicalise '%s'.", current), e);
        }
    }

    // TODO: Unit test this method
    public TestFile leftShift(Object content) {
        getParentFile().mkdirs();
        try {
            ResourceGroovyMethods.leftShift(this, content);
            return this;
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not append to test file '%s'", this), e);
        }
    }

    // TODO: Unit test this method
    public TestFile write(Object content) {
        try {
            if (!exists()) {
                final File parent = super.getParentFile();
                if (parent != null) {
                    if (!parent.mkdirs() && !parent.isDirectory()) {
                        throw new IOException("Directory '" + parent + "' could not be created");
                    }
                }
            }
            Files.write(toPath(), content.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not write to test file '%s'", this), e);
        }
        return this;
    }

    // TODO: Unit test this method
    public boolean isSelfOrDescendent(File file) {
        if (file.getAbsolutePath().equals(getAbsolutePath())) {
            return true;
        }
        return file.getAbsolutePath().startsWith(getAbsolutePath() + File.separatorChar);
    }

    @Override
    public TestFile getParentFile() {
        return super.getParentFile() == null ? null : new TestFile(super.getParentFile());
    }

    @Override
    public TestFile getAbsoluteFile() {
        return new TestFile(super.getAbsoluteFile());
    }

    public String getText() {
        assertIsFile();
        try {
            return new String(Files.readAllBytes(toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not read from test file '%s'", this), e);
        }
    }

    public void setText(String content) {
        getParentFile().mkdirs();
        try {
            ResourceGroovyMethods.setText(this, content);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Could not append to test file '%s'", this), e);
        }
    }

    public void copyTo(File target) {
        if (isDirectory()) {
            try {
                final Path targetDir = target.toPath();
                final Path sourceDir = this.toPath();
                Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attributes) throws IOException {
                        Path targetFile = targetDir.resolve(sourceDir.relativize(sourceFile));
                        Files.copy(sourceFile, targetFile, COPY_ATTRIBUTES, REPLACE_EXISTING);

                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
                        Path newDir = targetDir.resolve(sourceDir.relativize(dir));
                        Files.createDirectories(newDir);

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not copy test directory '%s' to '%s'", this, target), e);
            }
        } else {
            try {
                FileUtils.copyFile(this, target);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Could not copy test file '%s' to '%s'", this, target), e);
            }
        }
    }

    public boolean isSymbolicLink() {
        return Files.isSymbolicLink(toPath());
    }

    public ExecOutput exec(Object... args) {
        return new TestFileHelper(this).exec(asList(args));
    }

    public ExecOutput execWithFailure(List<?> args, List<?> env) {
        return new TestFileHelper(this).executeFailure(args, env);
    }

    public ExecOutput execute(List<?> args, List<?> env) {
        return new TestFileHelper(this).execute(args, env);
    }

    public void copyFrom(File target) {
        new TestFile(target).copyTo(this);
    }

    public void copyFrom(final URL resource) {
        final TestFile testFile = this;
        try {
            RetryUtil.retry(new Runnable() {
                public void run() {
                    try {
                        FileUtils.copyURLToFile(resource, testFile);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void unzipTo(File target) {
        assertIsFile();
        new TestFileHelper(this).unzipTo(target, useNativeTools);
    }

    public Snapshot snapshot() {
        assertIsFile();
        return new Snapshot(lastModified(), md5(this));
    }

    public static HashCode md5(File file) {
        HashFunction hf = Hashing.md5();
        HashingOutputStream hashingStream = new HashingOutputStream(hf, NullOutputStream.NULL_OUTPUT_STREAM);
        try {
            Files.copy(file.toPath(), hashingStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return hashingStream.hash();
    }

    public void assertHasChangedSince(Snapshot snapshot) {
        Snapshot now = snapshot();
        assertTrue(String.format("contents or modification time of %s have not changed", this), now.modTime != snapshot.modTime || !now.hash.equals(snapshot.hash));
    }

    public void assertContentsHaveChangedSince(Snapshot snapshot) {
        Snapshot now = snapshot();
        assertNotEquals(String.format("contents of %s have not changed", this), snapshot.hash, now.hash);
    }

    public void assertContentsHaveNotChangedSince(Snapshot snapshot) {
        Snapshot now = snapshot();
        assertEquals(String.format("contents of %s has changed", this), snapshot.hash, now.hash);
    }

    public void assertHasNotChangedSince(Snapshot snapshot) {
        Snapshot now = snapshot();
        assertEquals(String.format("last modified time of %s has changed", this), snapshot.modTime, now.modTime);
        assertEquals(String.format("contents of %s has changed", this), snapshot.hash, now.hash);
    }

    public static class Snapshot {
        private final long modTime;
        private final HashCode hash;

        public Snapshot(long modTime, HashCode hash) {
            this.modTime = modTime;
            this.hash = hash;
        }

        public long getModTime() {
            return modTime;
        }

        public HashCode getHash() {
            return hash;
        }
    }
}