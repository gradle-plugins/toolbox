/*
 * Copyright 2012 the original author or authors.
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


package dev.gradleplugins.test.fixtures.gradle.executer;

import org.gradle.internal.jvm.Jvm;
import org.gradle.util.TextUtil;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Check that the actual output lines match the expected output lines in content and order.
 */
public class SequentialOutputMatcher {
    public void assertOutputMatches(String expected, String actual, boolean ignoreExtraLines) {
        List<String> actualLines = normaliseOutput(readLines(actual)).stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());
        List<String> expectedLines = readLines(expected).stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());
        assertOutputLinesMatch(expectedLines, actualLines, ignoreExtraLines, actual);
    }

    private static List<String> readLines(String v) {
        return Arrays.asList(v.split("\\r?\\n", -1));
    }

    protected void assertOutputLinesMatch(List<String> expectedLines, List<String> actualLines, boolean ignoreExtraLines, String actual) {
        int pos = 0;
        for (; pos < actualLines.size() && pos < expectedLines.size(); pos++) {
            String expectedLine = expectedLines.get(pos);
            String actualLine = actualLines.get(pos);
            boolean matches = compare(expectedLine, actualLine);
            if (!matches) {
                if (expectedLine.contains(actualLine)) {
                    Assert.fail(String.format("Missing text at line %d.%nExpected: %s%nActual: %s%n---%nActual output:%n%s%n---", pos + 1, expectedLine, actualLine, actual));
                }
                if (actualLine.contains(expectedLine)) {
                    Assert.fail(String.format("Extra text at line %d.%nExpected: %s%nActual: %s%n---%nActual output:%n%s%n---", pos + 1, expectedLine, actualLine, actual));
                }
                Assert.fail(String.format("Unexpected value at line %d.%nExpected: %s%nActual: %s%n---%nActual output:%n$actual%n---", pos + 1, expectedLine, actualLine, actual));
            }
        }
        if (pos == actualLines.size() && pos < expectedLines.size()) {
            Assert.fail(String.format("Lines missing from actual result, starting at line %d.%nExpected: %s%nActual output:%n%s%n---", pos + 1, expectedLines.get(pos), actual));
        }
        if (!ignoreExtraLines && pos < actualLines.size() && pos == expectedLines.size()) {
            Assert.fail(String.format("Extra lines in actual result, starting at line %d.%nActual: %s%nActual output:%n%s%n---", pos + 1, actualLines.get(pos), actual));
        }
    }

    private List<String> normaliseOutput(List<String> lines) {
        if (lines.isEmpty()) {
            return lines;
        }
        boolean seenWarning = false;
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line.matches("Download .+")) {
                // ignore
            } else if (!seenWarning && !Jvm.current().getJavaVersion().isJava7Compatible() && line == "Support for reading or changing file permissions is only available on this platform using Java 7 or later.") {
                // ignore this warning once only on java < 7
                seenWarning = true;
            } else {
                result.add(line);
            }
        }
        return result;
    }


    protected boolean compare(String expected, String actual) {
        if (actual.equals(expected)) {
            return true;
        }

        if (expected.equals("Total time: 1 secs")) {
            return actual.matches("Total time: .+ secs");
        }

        // Normalise default object toString() values
        actual = actual.replaceAll("(\\w+(\\.\\w+)*)@\\p{XDigit}+", "$1@12345");
        // Normalise file separators
        actual = TextUtil.normaliseFileSeparators(actual);

        return actual.equals(expected);
    }
}
