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

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Files
import java.nio.file.LinkOption

abstract class AbstractTestFileSpec extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    protected File getTestDirectory() {
        // Ensure the type of testDirectory is a File implementation and not a TestFile.
        return new File(temporaryFolder.root.absolutePath)
    }

    protected TestFile of(File file) {
        return TestFile.of(file, LinkOption.NOFOLLOW_LINKS)
    }
}

@Subject(TestFile)
abstract class AbstractTestFileTest extends AbstractTestFileSpec {
    protected abstract File getNonExistingFileUnderTest()

    protected abstract File getExistingFileUnderTest()

    protected abstract File getNestedNonExistingFileUnderTest()

    def "can assert the existence of a file"() {
        when:
        def existingFile = getExistingFileUnderTest()
        of(existingFile).assertExists()

        then:
        noExceptionThrown()

        when:
        def nonExistingFile = getNonExistingFileUnderTest()
        of(nonExistingFile).assertExists()

        then:
        def error = thrown(AssertionError)
        error.message == "${nonExistingFile.absolutePath} does not exist"
    }

    def "can assert the non existance of a file"() {
        when:
        def existingFile = getExistingFileUnderTest()
        of(existingFile).assertDoesNotExist()

        then:
        def error = thrown(AssertionError)
        error.message == "${existingFile.absolutePath} should not exist"

        when:
        def nonExistingFile = getNonExistingFileUnderTest()
        of(nonExistingFile).assertDoesNotExist()

        then:
        noExceptionThrown()
    }
}

@Subject(TestFile)
class RegularFileTest extends AbstractTestFileTest {
    @Override
    protected File getNonExistingFileUnderTest() {
        def result = new File(testDirectory, 'aNonExistingFile.txt')
        assert !result.exists()
        assert result.parentFile.exists()
        return result
    }

    @Override
    protected File getExistingFileUnderTest() {
        def result = new File(testDirectory, 'aExistingFile.txt')
        assert result.createNewFile()
        assert result.exists()
        assert result.isFile()
        assert !Files.isSymbolicLink(result.toPath())
        assert result.parentFile.exists()
        return result
    }

    @Override
    protected File getNestedNonExistingFileUnderTest() {
        def result = new File(testDirectory, 'aNonExistingDirectory/aNonExistingFile.txt')
        assert !result.exists()
        assert !result.parentFile.exists()
        return result
    }

    def "can assert a file is indeed a file"() {
        given:
        def file = getExistingFileUnderTest()

        when:
        of(file).assertIsFile()

        then:
        noExceptionThrown()

        when:
        of(file).assertIsDirectory()

        then:
        def error1 = thrown(AssertionError)
        error1.message == "${file.absolutePath} is not a directory"

        when:
        of(file).assertIsSymbolicLink()

        then:
        def error2 = thrown(AssertionError)
        error2.message == "${file.absolutePath} is not a symbolic link"
    }

    def "can create file in existing directory"() {
        expect:
        def file = getExistingFileUnderTest()
        of(file).createFile()
        file.exists()
        file.isFile()
        !file.isDirectory()
        !Files.isSymbolicLink(file.toPath())
    }

    def "can create file in non existing directory"() {
        expect:
        def file = getNestedNonExistingFileUnderTest()
        of(file).createFile()
        file.exists()
        file.parentFile.exists()
        file.isFile()
        !file.isDirectory()
        !Files.isSymbolicLink(file.toPath())
    }

    def "can create file using path"() {
        expect:
        def file = getNonExistingFileUnderTest()
        of(testDirectory).createFile(file.name)
        file.exists()
        file.isFile()
        !file.isDirectory()
        !Files.isSymbolicLink(file.toPath())
    }

    def "can get the entire text content of a file"() {
        given:
        def file = getExistingFileUnderTest()
        file.text = 'Foo bar'

        expect:
        of(file).getText() == 'Foo bar'
    }

    def "can set the entire text content of a file"() {
        given:
        def file = getExistingFileUnderTest()

        when:
        of(file).setText('Foo bar')

        then:
        file.text == 'Foo bar'
    }

    def "will create missing file when setting the entire text content"() {
        given:
        def file = getNonExistingFileUnderTest()

        when:
        of(file).setText('Foo bar')

        then:
        file.exists()
        file.text == 'Foo bar'
    }

    def "will create missing directories when setting the entire text content"() {
        given:
        def file = getNestedNonExistingFileUnderTest()

        when:
        of(file).setText('Foo bar')

        then:
        file.exists()
        file.parentFile.exists()
        file.text == 'Foo bar'
    }
}

@Subject(TestFile)
class DirectoryTest extends AbstractTestFileTest {
    @Override
    protected File getNonExistingFileUnderTest() {
        def result = new File(testDirectory, 'aNonExistingDirectory')
        assert !result.exists()
        assert result.parentFile.exists()
        return result
    }

    @Override
    protected File getExistingFileUnderTest() {
        def result = new File(testDirectory, 'aExistingDirectory')
        assert result.mkdir()
        assert result.exists()
        assert result.isDirectory()
        assert !Files.isSymbolicLink(result.toPath())
        assert result.parentFile.exists()
        return result
    }

    @Override
    protected File getNestedNonExistingFileUnderTest() {
        def result = new File(testDirectory, 'aNonExistingDirectory/anotherNonExistingDirectory')
        assert !result.exists()
        assert !result.parentFile.exists()
        return result
    }

    def "can assert a directory is indeed a directory"() {
        given:
        def file = getExistingFileUnderTest()

        when:
        of(file).assertIsDirectory()

        then:
        noExceptionThrown()

        when:
        of(file).assertIsFile()

        then:
        def error1 = thrown(AssertionError)
        error1.message == "${file.absolutePath} is not a file"

        when:
        of(file).assertIsSymbolicLink()

        then:
        def error2 = thrown(AssertionError)
        error2.message == "${file.absolutePath} is not a symbolic link"
    }

    def "can create directory in existing directory"() {
        expect:
        def file = getNonExistingFileUnderTest()
        of(file).createDirectory()
        file.exists()
        !file.isFile()
        file.isDirectory()
        !Files.isSymbolicLink(file.toPath())
    }

    def "can create directory in non existing directory"() {
        expect:
        def file = getNestedNonExistingFileUnderTest()
        of(file).createDirectory()
        file.exists()
        file.parentFile.exists()
        !file.isFile()
        file.isDirectory()
        !Files.isSymbolicLink(file.toPath())
    }

    def "can create directory using path"() {
        expect:
        def file = getNonExistingFileUnderTest()
        of(testDirectory).createDirectory(file.name)
        file.exists()
        !file.isFile()
        file.isDirectory()
        !Files.isSymbolicLink(file.toPath())
    }

    @Ignore
    def "can assert a directory is empty"() {
        def file = getExistingFileUnderTest()

        when:
        of(file).assertIsEmptyDirectory()

        then:
        noExceptionThrown()

        when:
        def fooFile = of(file).createFile("foo.txt")
        of(file).assertIsEmptyDirectory()

        then:
        def error1 = thrown(AssertionError)
        error1.message == "For dir: ${file.absolutePath}\n extra files: [foo.txt], missing files: [], expected: [] expected:<[]> but was:<[foo.txt]>"

        when:
        of(fooFile).assertIsEmptyDirectory()

        then:
        def error2 = thrown(AssertionError)
        error2.message == "${fooFile.absolutePath} is not a directory"
    }

    def "assert a file when getting the text content of a test file"() {
        given:
        def file = getExistingFileUnderTest()

        when:
        of(file).getText()

        then:
        def error = thrown(AssertionError)
        error.message == "${file.absolutePath} is not a file"
    }
}

@Subject(TestFile)
abstract class AbstractSymbolicLinkTest extends AbstractTestFileTest {
    @Override
    protected File getNonExistingFileUnderTest() {
        def result = new File(testDirectory, 'aNonExistingLink.txt')
        assert !result.exists()
        assert result.parentFile.exists()
        return result
    }

    @Override
    protected File getExistingFileUnderTest() {
        def result = new File(testDirectory, 'aExistingLink.txt')
        Files.createSymbolicLink(result.toPath(), targetFile.toPath())
        assert Files.isSymbolicLink(result.toPath())
        return result
    }

    @Override
    protected File getNestedNonExistingFileUnderTest() {
        def result = new File(testDirectory, 'aNonExistingDirectory/aNonExistingLink.txt')
        assert !result.exists()
        assert !result.parentFile.exists()
        return result
    }

    protected abstract File getTargetFile();

    protected File getNonExistingTargetFile() {
        def target = new File(testDirectory, "non.existing.target")
        assert !target.exists()
        assert !Files.isSymbolicLink(target.toPath())
        return target
    }

    def "can create symbolic link in existing directory"() {
        expect:
        def file = getNonExistingFileUnderTest()
        of(file).createSymbolicLink(getTargetFile())
        of(file).isSymbolicLink()
        file.exists()
        Files.isSymbolicLink(file.toPath())
    }

    def "can create symbolic link in non existing directory"() {
        expect:
        def file = getNestedNonExistingFileUnderTest()
        of(file).createSymbolicLink(getTargetFile())
        of(file).isSymbolicLink()
        file.exists()
        file.parentFile.exists()
        Files.isSymbolicLink(file.toPath())
    }

    def "can create symbolic link to non existing target"() {
        expect:
        def file = getNonExistingFileUnderTest()
        of(file).createSymbolicLink(getNonExistingTargetFile())
        of(file).isSymbolicLink()
        !file.exists()
        Files.isSymbolicLink(file.toPath())
    }

    def "can create symbolic link using path to target"() {
        expect:
        def file = getNonExistingFileUnderTest()
        of(file).createSymbolicLink(targetFile.name)
        of(file).isSymbolicLink()
        file.exists()
        Files.isSymbolicLink(file.toPath())
    }

    def "can assert a symbolic link is indeed a symbolic link"() {
        given:
        def file = getExistingFileUnderTest()

        when:
        of(file).assertIsSymbolicLink()

        then:
        noExceptionThrown()
    }
}

@Subject(TestFile)
class SymbolicLinkToFileTest extends AbstractSymbolicLinkTest {
    @Override
    protected File getTargetFile() {
        def target = new File(testDirectory, 'aTarget.txt')
        assert target.createNewFile()
        assert target.exists()
        assert target.isFile()
        return target
    }
}

@Subject(TestFile)
class SymbolicLinkToDirectoryTest extends AbstractSymbolicLinkTest {
    @Override
    protected File getTargetFile() {
        def target = new File(testDirectory, 'aTarget')
        assert target.mkdir()
        assert target.exists()
        assert target.isDirectory()
        return target
    }
}

@Subject(TestFile)
class TestFileTest extends AbstractTestFileSpec {

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

    @Ignore("Implementing this cause TestKit to fail and starting 100s of Java processes")
    def "returns a TestFile from TestFile#getCanonicalFile()"() {
        expect:
        def file = new TestFile(testDirectory, "aFile.txt")
        file.getCanonicalFile() instanceof TestFile
    }
}
