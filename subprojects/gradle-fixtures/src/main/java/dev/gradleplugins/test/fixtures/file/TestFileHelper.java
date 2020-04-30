package dev.gradleplugins.test.fixtures.file;

import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.codehaus.groovy.util.ComplexKeyHashMap;
import org.codehaus.groovy.util.SingleKeyHashMap;
import org.hamcrest.Matchers;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestFileHelper {
    private final TestFile file;

    TestFileHelper(TestFile file) {
        this.file = file;
    }

    public void unzipTo(File target, boolean nativeTools) {
        // Check that each directory in hierarchy is present
        try (InputStream instr = new FileInputStream(file)) {
            Set<String> dirs = new HashSet<>();
            try (ZipInputStream zipStr = new ZipInputStream(instr)) {
                ZipEntry entry;
                while ((entry = zipStr.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        assertTrue("Duplicate directory '" + entry.getName() + "'", dirs.add(entry.getName()));
                    }
                    if (!entry.getName().contains("/")) {
                        continue;
                    }
                    String parent = StringUtils.substringBeforeLast(entry.getName(), "/") + "/";
                    assertTrue("Missing dir '" + parent + "'", dirs.contains(parent));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (nativeTools && isUnix()) {
            try {
                // TODO: `inheritIO` is technically not correct here. We only want to inherit stdout and stderr.
                Process process = new ProcessBuilder().command("unzip", "-q", "-o", file.getAbsolutePath(), "-d", target.getAbsolutePath()).inheritIO().start();
                assertThat(process.waitFor(), equalTo(0));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        Expand unzip = new Expand();
        unzip.setSrc(file);
        unzip.setDest(target);

        unzip.setProject(new Project());
        unzip.execute();
    }

    private boolean isUnix() {
        return !SystemUtils.IS_OS_WINDOWS;
    }

    public ExecOutput exec(List<?> args) {
        return execute(args, null);
    }

    public ExecOutput execute(List<?> args, List<?> env) {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(file.getAbsolutePath());
        commandLine.addAll(args.stream().map(Object::toString).collect(Collectors.toList()));
        ProcessBuilder processBuilder = new ProcessBuilder().command(commandLine);
        Map<String, String> environment = env.stream().map(TestFileHelper::toEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        processBuilder.environment().putAll(environment);
        try {
            Process process = processBuilder.start();

            // Prevent process from hanging by consuming the output as we go.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ByteArrayOutputStream error = new ByteArrayOutputStream();

            Thread outputThread = new Thread(() -> {
                try {
                    ByteStreams.copy(process.getInputStream(), output);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            outputThread.start();
            Thread errorThread = new Thread(() -> {
                try {
                    ByteStreams.copy(process.getErrorStream(), error);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            errorThread.start();

            try {
                int exitCode = process.waitFor();
                outputThread.join();
                errorThread.join();
                return new ExecOutput(exitCode, output.toString(), error.toString());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map.Entry<String, String> toEntry(Object o) {
        String[] tokens = o.toString().split("=", -1);
        assertThat(tokens.length, Matchers.equalTo(2));
        return new HashMap.SimpleEntry<>(tokens[0], tokens[1]);
    }

    public ExecOutput executeSuccess(List<?> args, List<?> env) {
        ExecOutput result = execute(args, env);
        if (result.getExitCode() != 0) {
            throw new RuntimeException(String.format("Could not execute %s. Error: %s, Output: %s", file.getAbsolutePath(), result.getError(), result.getOut()));
        }
        return result;
    }

    public ExecOutput executeFailure(List<?> args, List<?> env) {
        ExecOutput result = execute(args, env);
        if (result.getExitCode() == 0) {
            throw new RuntimeException(String.format("Unexpected success, executing %s. Error: %s, Output: %s", file.getAbsolutePath(), result.getError(), result.getOut()));
        }
        return result;
    }
}
