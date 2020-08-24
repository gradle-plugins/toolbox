package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_IsEmptyDirectoryTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns true if directory is empty"() {
        expect:
        FileSystemUtils.isEmptyDirectory(temporaryFolder.root)
    }

    def "throws exception if non directory specified"() {
        given:
        def file = temporaryFolder.newFile('file')

        when:
        FileSystemUtils.isEmptyDirectory(file)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not check if directory is empty because '${file.absolutePath}' is a file."
    }

    def "throws exception if directory argument is null"() {
        when:
        FileSystemUtils.isEmptyDirectory(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Could not check if directory is empty because the specified directory is null.'
    }

    def "returns false if directory contains files"() {
        given:
        temporaryFolder.newFile('foo')

        expect:
        !FileSystemUtils.isEmptyDirectory(temporaryFolder.root)
    }

    def "returns false if directory contains nested files"() {
        given:
        temporaryFolder.newFolder('dir1', 'dir2')
        temporaryFolder.newFile('dir1/dir2/foo')

        expect:
        !FileSystemUtils.isEmptyDirectory(temporaryFolder.root)
    }

    def "returns true if directory contains only empty directories"() {
        given:
        temporaryFolder.newFolder('dir1', 'dir2')

        expect:
        FileSystemUtils.isEmptyDirectory(temporaryFolder.root)
    }
}
