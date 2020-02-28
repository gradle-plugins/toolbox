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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import org.hamcrest.Matcher;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

class ArchiveTestFixture {
    private final ListMultimap<String, String> filesByRelativePath = LinkedListMultimap.create();
    private final ListMultimap<String, Integer> fileModesByRelativePath = ArrayListMultimap.create();

    protected void add(String relativePath, String content) {
        filesByRelativePath.put(relativePath, content);
    }

    protected void addMode(String relativePath, int mode) {
        fileModesByRelativePath.put(relativePath, mode & 0777);
    }

    public ArchiveTestFixture assertFileMode(String relativePath, int fileMode) {
        List<Integer> modes = fileModesByRelativePath.get(relativePath);
        assert modes.size() == 1;
        assertThat(modes.get(0), equalTo(fileMode));
        return this;
    }

    public ArchiveTestFixture assertContainsFile(String relativePath) {
        assert filesByRelativePath.keySet().contains(relativePath);
        return this;
    }

    public ArchiveTestFixture assertNotContainsFile(String relativePath) {
        assert !filesByRelativePath.keySet().contains(relativePath);
        return this;
    }

    public ArchiveTestFixture assertContainsFile(String relativePath, int occurrences) {
        assertContainsFile(relativePath);
        int actualOccurrences = filesByRelativePath.get(relativePath).size();
        String failureMessage = String.format("Incorrect count for file '%s': expected %s, got %s", relativePath, occurrences, actualOccurrences);
        assertEquals(failureMessage, occurrences, actualOccurrences);
        return this;
    }

    public String content(String relativePath) {
        List<String> files = filesByRelativePath.get(relativePath);
        assert files.size() == 1;
        return files.get(0);
    }

    public Integer countFiles(String relativePath) {
        return filesByRelativePath.get(relativePath).size();
    }

    public ArchiveTestFixture hasDescendants(String... relativePaths) {
        return hasDescendants(Arrays.asList(relativePaths));
    }

    public ArchiveTestFixture hasDescendants(Collection<String> relativePaths) {
        assertThat(filesByRelativePath.keySet(), equalTo(new HashSet<>(relativePaths)));
        ListMultimap<String, String> expectedCounts = ArrayListMultimap.create();
        for (String fileName : relativePaths) {
            expectedCounts.put(fileName, fileName);
        }
        for (String fileName : relativePaths) {
            assertEquals(expectedCounts.get(fileName).size(), filesByRelativePath.get(fileName).size());
        }
        return this;
    }

    public ArchiveTestFixture hasDescendantsInOrder(String... relativePaths) {
        List<String> expectedOrder = Arrays.asList(relativePaths);
        List<String> actualOrder = new ArrayList<>(filesByRelativePath.keySet());
        assertEquals(actualOrder, expectedOrder);
        return this;
    }

    public ArchiveTestFixture containsDescendants(String... relativePaths) {
        for (String path : relativePaths) {
            assertContainsFile(path);
        }
        return this;
    }

    public ArchiveTestFixture doesNotContainDescendants(String... relativePaths) {
        for (String path : relativePaths) {
            assertNotContainsFile(path);
        }
        return this;
    }

    /**
     * Asserts that there is exactly one file present with the given path, and that this file has the given content.
     */
    public ArchiveTestFixture assertFileContent(String relativePath, String fileContent) {
        return assertFileContent(relativePath, equalTo(fileContent));
    }

    public ArchiveTestFixture assertFileContent(String relativePath, Matcher contentMatcher) {
        assertThat(content(relativePath), contentMatcher);
        return this;
    }

    /**
     * Asserts that there is a file present with the given path and content.
     */
    public ArchiveTestFixture assertFilePresent(String relativePath, String fileContent) {
        assertThat(filesByRelativePath.get(relativePath), hasItem(fileContent));
        return this;
    }
}
