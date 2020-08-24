package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_CreateFileTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns created file"() {
        given:
        def file = new File(temporaryFolder.root, 'file')

        when:
        def result = FileSystemUtils.createFile(file)

        then:
        result instanceof File
        result.absolutePath == "${temporaryFolder.root}/file"
    }

    def "creates missing parent directory"() {
        given:
        def file = new File(temporaryFolder.root, 'missing-dir/file')

        when:
        FileSystemUtils.createFile(file)

        then:
        file.parentFile.exists()
    }

    def "creates all missing parent directories"() {
        given:
        def file = new File(temporaryFolder.root, 'missing-dir1/missing-dir2/file')

        when:
        FileSystemUtils.createFile(file)

        then:
        file.parentFile.exists()
        file.parentFile.directory
    }

    def "creates missing file"() {
        given:
        def file = new File(temporaryFolder.root, 'file')

        when:
        FileSystemUtils.createFile(file)

        then:
        file.exists()
        file.file
    }

    def "can create existing file"() {
        given:
        def file = temporaryFolder.newFile('file')

        when:
        FileSystemUtils.createFile(file)

        then:
        file.exists()
        file.file
    }

    def "throws uncheck exception when file creation fails"() {
        given:
        def file = new ThrowingCreateNewFileFile(temporaryFolder.root, 'file')

        when:
        FileSystemUtils.createFile(file)

        then:
        def ex = thrown(UncheckedIOException)
        ex.message == "Could not create file at '${temporaryFolder.root}/file'."

        and:
        !file.exists()
    }

    def "throws exception when file is a directory"() {
        given:
        def directory = temporaryFolder.newFolder('dir')

        when:
        FileSystemUtils.createFile(directory)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not create file because '${temporaryFolder.root}/dir' is a directory."
    }

    def "throws exception if file to create is null"() {
        when:
        FileSystemUtils.createFile(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not create file because the specified file is null."
    }

    private static final class ThrowingCreateNewFileFile extends File {
        ThrowingCreateNewFileFile(File parent, String child) {
            super(parent, child)
        }

        @Override
        boolean createNewFile() throws IOException {
            throw new IOException()
        }
    }
}
