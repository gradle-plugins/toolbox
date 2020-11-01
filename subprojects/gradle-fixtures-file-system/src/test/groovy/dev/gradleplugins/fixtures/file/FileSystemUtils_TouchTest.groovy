package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_TouchTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "create file when missing"() {
        given:
        def file = new File(temporaryFolder.root, 'foo')

        when:
        FileSystemUtils.touch(file)

        then:
        file.exists()
    }

    def "create parent directories when missing"() {
        given:
        def file = new File(temporaryFolder.root, 'foo/bar/far')

        when:
        FileSystemUtils.touch(file)

        then:
        file.exists()
    }

    def "returns the specified file instance"() {
        given:
        def file = new File(temporaryFolder.root, 'foo')

        expect:
        FileSystemUtils.touch(file) == file
    }
}
