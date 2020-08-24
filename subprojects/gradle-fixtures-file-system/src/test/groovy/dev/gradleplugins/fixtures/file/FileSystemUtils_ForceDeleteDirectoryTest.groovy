package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject
import spock.util.environment.OperatingSystem

import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermissions
import java.util.function.Consumer

@Subject(FileSystemUtils)
class FileSystemUtils_ForceDeleteDirectoryTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns deleted directory"() {
        given:
        def directory = temporaryFolder.newFolder('directory')

        when:
        def result = FileSystemUtils.forceDeleteDirectory(directory)

        then:
        result instanceof File
        result.absolutePath == "${temporaryFolder.root}/directory"
    }

    def "can delete empty directory"() {
        given:
        def directory = temporaryFolder.newFolder('directory')

        when:
        FileSystemUtils.forceDeleteDirectory(directory)

        then:
        !directory.exists()
    }

    def "throws exception if directory to delete is a file"() {
        given:
        def file = temporaryFolder.newFile('file')

        when:
        FileSystemUtils.forceDeleteDirectory(file)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not delete directory because '${file.absolutePath}' is a file."
    }

    def "throws exception if directory to delete is null"() {
        when:
        FileSystemUtils.forceDeleteDirectory(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not delete directory because the specified directory is null."
    }

    def "can delete non-empty directory"() {
        given:
        def directory = temporaryFolder.newFolder('directory')
        temporaryFolder.newFile('directory/foo')
        temporaryFolder.newFile('directory/bar')

        when:
        FileSystemUtils.forceDeleteDirectory(directory)

        then:
        !directory.exists()
    }

    def "can delete nested directories"() {
        given:
        def directory = temporaryFolder.newFolder('directory')
        temporaryFolder.newFile('directory/foo')
        temporaryFolder.newFile('directory/bar')

        and:
        temporaryFolder.newFolder('directory', 'nested')
        temporaryFolder.newFile('directory/nested/foo')
        temporaryFolder.newFile('directory/nested/bar')

        when:
        FileSystemUtils.forceDeleteDirectory(directory)

        then:
        !directory.exists()
    }

    @Requires({ OperatingSystem.current.windows })
    def "throws exception with all the paths that could not be deleted"() {
        given:
        def directory = temporaryFolder.newFolder('directory')
        def foo = temporaryFolder.newFile('directory/foo')
        temporaryFolder.newFile('directory/bar')

        and:
        temporaryFolder.newFolder('directory', 'nested')
        temporaryFolder.newFile('directory/nested/foo')
        def bar = temporaryFolder.newFile('directory/nested/bar')

        when:
        lock(foo) {
            lock(bar) {
                FileSystemUtils.forceDeleteDirectory(directory)
            }
        }

        then:
        def ex = thrown(RuntimeException)
        ex.message == """Unable to recursively delete directory '${directory.canonicalPath}', failed paths:
\t- ${directory.canonicalPath}/foo
\t- ${directory.canonicalPath}/nested/bar
\t- ${directory.canonicalPath}/nested
\t- ${directory.canonicalPath}
"""
    }

    private static void lock(File file, Runnable run) {
        new RandomAccessFile(file, "rw").withCloseable {randomFile ->
            randomFile.getChannel().withCloseable {channel ->
                channel.lock().withCloseable {lock ->
                    run.run()
                }
            }
        }
    }
}
