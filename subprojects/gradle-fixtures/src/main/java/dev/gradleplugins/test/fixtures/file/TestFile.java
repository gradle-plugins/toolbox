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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.tools.ant.util.TeeOutputStream;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.gradle.internal.hash.HashCode;
import org.gradle.internal.hash.Hashing;
import org.gradle.internal.hash.HashingOutputStream;
import org.hamcrest.Matchers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestFile extends File {
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
        if (Arrays.asList(options).contains(LinkOption.NOFOLLOW_LINKS)) {
            return new TestFile(file, false);
        }
        return new TestFile(file);
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
        Set<String> expected = new TreeSet<String>(Arrays.asList(descendants));

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

    public boolean isSymbolicLink() {
        return Files.isSymbolicLink(toPath());
    }

    public ExecOutput exec(Object... args) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.addArguments(Arrays.toString(args));
        Executor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(new TeeOutputStream(stdout, System.out), new TeeOutputStream(stderr, System.err)));
        executor.setExitValue(0);
        try {
            int exitCode = executor.execute(commandLine);
            return new ExecOutput(exitCode, stdout.toString(), stderr.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ExecOutput execWithFailure(List<Object> args, List<Object> env) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.addArguments(Arrays.toString(args.toArray(new Object[0])));
        Executor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(new TeeOutputStream(stdout, System.out), new TeeOutputStream(stderr, System.err)));
        try {
            Map<String, String> environment = env.stream().map(TestFile::toEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            int exitCode = executor.execute(commandLine, environment);
            assertThat(exitCode, Matchers.not(Matchers.equalTo(0)));
            return new ExecOutput(exitCode, stdout.toString(), stderr.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map.Entry<String, String> toEntry(Object o) {
        String[] tokens = o.toString().split("=");
        assertThat(tokens.length, Matchers.equalTo(2));
        return new HashMap.SimpleEntry<>(tokens[0], tokens[1]);
    }

    public ExecOutput execute(List<Object> args, List<Object> env) {
        CommandLine commandLine = new CommandLine(this);
        commandLine.addArguments(args.stream().map(Objects::toString).collect(Collectors.toList()).toArray(new String[0]));
        Executor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(new TeeOutputStream(stdout, System.out), new TeeOutputStream(stderr, System.err)));
        try {
            Map<String, String> environment = env.stream().map(TestFile::toEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            int exitCode = executor.execute(commandLine, environment);
            assertThat(exitCode, Matchers.equalTo(0));
            return new ExecOutput(exitCode, stdout.toString(), stderr.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Snapshot snapshot() {
        assertIsFile();
        return new Snapshot(lastModified(), md5(this));
    }

    public static HashCode md5(File file) {
        HashingOutputStream hashingStream = Hashing.primitiveStreamHasher();
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