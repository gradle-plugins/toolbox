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

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Checks that all lines contained in the expected output are present in the actual output, in any order.
 */
public class AnyOrderOutputMatcher extends SequentialOutputMatcher {
    protected void assertOutputLinesMatch(List<String> expectedLines, List<String> actualLines, boolean ignoreExtraLines, String actual) {
        List<String> unmatchedLines = new ArrayList<String>(actualLines);
        expectedLines.removeIf(String::isEmpty);
        unmatchedLines.removeIf(String::isEmpty);

        expectedLines.forEach( expectedLine -> {
            List<String> matchedLine = unmatchedLines.stream().filter(actualLine -> {
                return compare(expectedLine, actualLine);
            }).collect(Collectors.toList());
            if (!matchedLine.isEmpty()) {
                unmatchedLines.removeAll(matchedLine);
            } else {
                Assert.fail(String.format("Line missing from output.%n%s%n---%nActual output:%n%s%n---", expectedLine, actual));
            }
        });

        if (!(ignoreExtraLines || unmatchedLines.isEmpty())) {
            String unmatched = String.join(System.lineSeparator(), unmatchedLines);
            Assert.fail(String.format("Extra lines in output.%n%s%n---%nActual output:%n%s%n---", unmatched, actual));
        }
    }
}
