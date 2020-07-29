package dev.gradleplugins.test.fixtures.sources

import dev.gradleplugins.test.fixtures.file.TestFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SourceFileTest extends Specification {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    def sourceFileUnderTest = new SourceFile('cpp', 'main.cpp', '''
            |#include <iostream>
            |
            |int main() {
            |    std::cout << "Hello world!" << std::endl;
            |    return 0;
            |}
            |'''.stripMargin())

    def "has meaningful toString implementation"() {
        expect:
        sourceFileUnderTest.toString() == "SourceFile{path='cpp', name='main.cpp', content='#include <iostream>...'}"
    }

    def "can write to directory"() {
        when:
        sourceFileUnderTest.writeToDirectory(temporaryFolder.root)

        then:
        TestFile.of(temporaryFolder.root).assertHasDescendants('main.cpp')
        TestFile.of(temporaryFolder.root).file('main.cpp').text == sourceFileUnderTest.content
    }

    def "can write to file"() {
        when:
        sourceFileUnderTest.writeToFile(new File(temporaryFolder.root, 'some-file.cc'))

        then:
        TestFile.of(temporaryFolder.root).assertHasDescendants('some-file.cc')
        TestFile.of(temporaryFolder.root).file('some-file.cc').text == sourceFileUnderTest.content
    }
}
