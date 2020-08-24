package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_GetDescendantsTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns empty set when folder is empty"() {
        expect:
        FileSystemUtils.getDescendants(temporaryFolder.root) == [] as Set
    }

    def "returns files in folder"() {
        given:
        temporaryFolder.newFile('foo')
        temporaryFolder.newFile('bar')

        expect:
        FileSystemUtils.getDescendants(temporaryFolder.root) == ['foo', 'bar'] as Set
    }

    def "ignores empty directories"() {
        given:
        temporaryFolder.newFile('foo')
        temporaryFolder.newFile('bar')
        temporaryFolder.newFolder('dir1')
        temporaryFolder.newFolder('dir1', 'dir2')

        expect:
        FileSystemUtils.getDescendants(temporaryFolder.root) == ['foo', 'bar'] as Set
    }

    def "includes files in sub-directories"() {
        given:
        temporaryFolder.newFile('foo')
        temporaryFolder.newFolder('dir1')
        temporaryFolder.newFile('dir1/foo')
        temporaryFolder.newFolder('dir1', 'dir2')
        temporaryFolder.newFile('dir1/dir2/foo')

        expect:
        FileSystemUtils.getDescendants(temporaryFolder.root) == ['foo', 'dir1/foo', 'dir1/dir2/foo'] as Set
    }

    def "throws exception if IO exception while listing directories"() {
        given:
        def directory = new AlwaysReturnNullFromListFilesFile(temporaryFolder.root.absolutePath)

        when:
        FileSystemUtils.getDescendants(directory)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "Could not visit folder '${temporaryFolder.root.absolutePath}' because an error occurred."
    }

    def "throws exception if root directory is a file"() {
        given:
        def file = temporaryFolder.newFile('file')

        when:
        FileSystemUtils.getDescendants(file)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not gather descendants because '${file.absolutePath}' is a file."
    }

    def "throws exception if root directory does not exists"() {
        given:
        def directory = new File(temporaryFolder.root, 'missing-directory')

        when:
        FileSystemUtils.getDescendants(directory)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not gather descendants because '${directory.absolutePath}' does not exists."
    }

    private static final class AlwaysReturnNullFromListFilesFile extends File {
        AlwaysReturnNullFromListFilesFile(String pathname) {
            super(pathname)
        }

        @Override
        File[] listFiles() {
            return null
        }
    }
}
