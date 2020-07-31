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

package dev.gradleplugins.spock.lang;

import dev.gradleplugins.test.fixtures.util.RetryUtil;
import org.apache.commons.lang3.SystemUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * A JUnit rule which provides a unique temporary folder for the test.
 */
// TODO: Can we move this to an internal API?
abstract class AbstractTestDirectoryProvider implements TestRule, TestDirectoryProvider {
    protected final File root;
    protected final String className;

    private static final Random RANDOM = new Random();
    private static final int ALL_DIGITS_AND_LETTERS_RADIX = 36;
    private static final int MAX_RANDOM_PART_VALUE = Integer.valueOf("zzzzz", ALL_DIGITS_AND_LETTERS_RADIX);
    private static final Pattern WINDOWS_RESERVED_NAMES = Pattern.compile("(con)|(prn)|(aux)|(nul)|(com\\d)|(lpt\\d)", Pattern.CASE_INSENSITIVE);

    private File dir;
    private String prefix;
    private boolean cleanup = true;

    public AbstractTestDirectoryProvider(File root, Class<?> testClass) {
        this.root = root;
        String safeClassName = testClass.getSimpleName();
        // Windows is annoying with filename too long, let's restrict the class name as well.
        if (SystemUtils.IS_OS_WINDOWS && safeClassName.length() > 20) {
            safeClassName = safeClassName.substring(0, 10) + "..." + safeClassName.substring(safeClassName.length() - 9);
        }
        this.className = safeClassName;
    }

    @Override
    public void suppressCleanup() {
        cleanup = false;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    public void cleanup() {
        if (cleanup && dir != null && dir.exists()) {
            try {
                RetryUtil.retry(100, Duration.ofMillis(100), () -> {
                    try {
                        FileUtils.forceDeleteDirectory(dir);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Statement apply(final Statement base, Description description) {
        init(description.getMethodName());

        return new TestDirectoryCleaningStatement(base, description);
    }

    private class TestDirectoryCleaningStatement extends Statement {
        private final Statement base;
        private final Description description;

        TestDirectoryCleaningStatement(Statement base, Description description) {
            this.base = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            // implicitly don't clean up if this throws
            base.evaluate();

            try {
                cleanup();
            } catch (Exception e) {
                throw new RuntimeException(cleanupErrorMessage(), e);
            }
        }

        // NOTE: LeaksFileHandles not supported

        private String cleanupErrorMessage() {
            return "Couldn't delete test dir for `" + displayName() + "` (test is holding files open). "
            + "In order to find out which files are held open you may find http://file-leak-detector.kohsuke.org/ useful.";
        }

        private String displayName() {
            return description.getDisplayName();
        }
    }

    protected void init(String methodName) {
        if (methodName == null) {
            // must be a @ClassRule; use the rule's class name instead
            methodName = getClass().getSimpleName();
        }
        if (prefix == null) {
            String safeMethodName = methodName.replaceAll("[^\\w]", "_");
            if (safeMethodName.length() > 30) {
                safeMethodName = safeMethodName.substring(0, 19) + "..." + safeMethodName.substring(safeMethodName.length() - 9);
            }
            prefix = String.format("%s/%s", className, safeMethodName);
        }
    }

    @Override
    public File getTestDirectory() {
        if (dir == null) {
            dir = createUniqueTestDirectory();
        }
        return dir;
    }

    private File createUniqueTestDirectory() {
        while (true) {
            // Use a random prefix to avoid reusing test directories
            String randomPrefix = Integer.toString(RANDOM.nextInt(MAX_RANDOM_PART_VALUE), ALL_DIGITS_AND_LETTERS_RADIX);
            if (WINDOWS_RESERVED_NAMES.matcher(randomPrefix).matches()) {
                continue;
            }
            File dir = new File(root, String.format("%s/%s", getPrefix(), randomPrefix));
            if (dir.mkdirs()) {
                return dir;
            }
        }
    }

    private String getPrefix() {
        if (prefix == null) {
            // This can happen if this is used in a constructor or a @Before method. It also happens when using
            // @RunWith(SomeRunner) when the runner does not support rules.
            prefix = className;
        }
        return prefix;
    }

    public File file(String... path) {
        return FileUtils.file(getTestDirectory(), path);
    }

    public File createFile(String... path) {
        return FileUtils.createFile(file(path));
    }

    public File createDirectory(String... path) {
        return FileUtils.createDirectory(file(path));
    }
}