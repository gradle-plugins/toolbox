package dev.gradleplugins.test.fixtures.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
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
}
