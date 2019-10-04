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

package dev.gradleplugins.test.fixtures.file

import dev.gradleplugins.test.fixtures.file.TestFile
import org.gradle.api.internal.file.TemporaryFileProvider
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(TestFile)
class TestFileTest extends Specification {
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

    File getTestDirectory() {
        // Ensure the type of testDirectory is a File implementation and not a TestFile.
        return new File(temporaryFolder.testDirectory.absolutePath)
    }

    TestFile of(File file) {
        return new TestFile(file);
    }

    def "can assert a file is indeed a file"() {
        when:
        def file = new File(testDirectory, 'aFile.txt')

        then:
        file.createNewFile()
        file.isFile()
        !file.isDirectory()

        when:
        of(file).assertIsFile()

        then:
        noExceptionThrown()

        when:
        of(file).assertIsDir()

        then:
        def error = thrown(AssertionError)
        error.message == "${file.absolutePath} is not a directory."
    }

    def "can create file in existing directory"() {
        expect:
        def file = new TestFile(testDirectory, "aFile.txt")
        !file.exists()
        file.createFile()
        file.exists()
        file.isFile()
    }

    // TODO: What about symbolic link
    def "can assert the existance of a file or directory"() {
        given:
        def file = new File(testDirectory, 'aFile.txt')
        assert file.createNewFile()

        when:
        of(file).assertExists()

        then:
        noExceptionThrown()

        when:
        of(file.parentFile).assertExists()

        then:
        noExceptionThrown()

        when:
        def nonExisting = new File(testDirectory, 'nonExisting')
        assert !nonExisting.exists()
        of(nonExisting).assertExists()

        then:
        def error = thrown(AssertionError)
        error.message == "${nonExisting.absoluteFile} does not exist"
    }

    def "can create existing file"() {
        given:
        def file = new File(testDirectory, 'aFile.txt')
        file.createNewFile()
        of(file).assertExs
        of(file).createFile()
    }

    // Avoid downgrading File APIs
    def "returns a TestFile from TestFile#getParentFile()"() {
        expect:
        def file = new TestFile(testDirectory, "aFile.txt")
        file.getParentFile() instanceof TestFile
        new TestFile("/").parentFile == null
    }

    def "returns a TestFile from TestFile#getAbsoluteFile()"() {
        expect:
        def file = new TestFile(testDirectory, "aFile.txt")
        file.getAbsoluteFile() instanceof TestFile
    }
}
