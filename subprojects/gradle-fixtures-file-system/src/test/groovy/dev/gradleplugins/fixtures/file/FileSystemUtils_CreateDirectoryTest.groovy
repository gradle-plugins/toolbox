package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_CreateDirectoryTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns created directory"() {
        given:
        def directory = new File(temporaryFolder.root, 'directory')

        when:
        def result = FileSystemUtils.createDirectory(directory)

        then:
        result instanceof File
        result.absolutePath == "${temporaryFolder.root}/directory"
    }

    def "creates missing parent directory"() {
        given:
        def directory = new File(temporaryFolder.root, 'missing-dir/directory')

        when:
        FileSystemUtils.createDirectory(directory)

        then:
        directory.parentFile.exists()
        directory.parentFile.directory
    }

    def "creates all missing parent directories"() {
        given:
        def directory = new File(temporaryFolder.root, 'missing-dir1/missing-dir2/directory')

        when:
        FileSystemUtils.createDirectory(directory)

        then:
        directory.parentFile.exists()
        directory.parentFile.directory
    }

    def "creates missing directory"() {
        given:
        def directory = new File(temporaryFolder.root, 'directory')

        when:
        FileSystemUtils.createDirectory(directory)

        then:
        directory.exists()
        directory.directory
    }

    def "can create existing directory"() {
        given:
        def directory = temporaryFolder.newFolder('directory')

        when:
        FileSystemUtils.createDirectory(directory)

        then:
        directory.exists()
        directory.directory
    }

    def "throws exception when directory is a file"() {
        given:
        def file = temporaryFolder.newFile('file')

        when:
        FileSystemUtils.createDirectory(file)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not create directory because '${temporaryFolder.root}/file' is a file."
    }

    def "throws exception if directory to create is null"() {
        when:
        FileSystemUtils.createDirectory(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not create directory because the specified directory is null."
    }

    def "throws exception if some directories was not created"() {
        given:
        def directory = new AlwaysReturnsFalseFromMkdirsFile(temporaryFolder.root, 'dir1/dir2/dir3')

        when:
        FileSystemUtils.createDirectory(directory)

        then:
        def ex = thrown(RuntimeException)
        ex.message == "Could not create directory because some parent directory could not be created."
    }
    
    private static final class AlwaysReturnsFalseFromMkdirsFile extends File {
        AlwaysReturnsFalseFromMkdirsFile(File parent, String child) {
            super(parent, child)
        }

        @Override
        boolean mkdirs() {
            return false;
        }
    }
}
