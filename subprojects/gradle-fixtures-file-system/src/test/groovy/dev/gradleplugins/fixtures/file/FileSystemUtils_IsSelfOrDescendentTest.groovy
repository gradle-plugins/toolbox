package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_IsSelfOrDescendentTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns true for the same file"() {
        expect:
        FileSystemUtils.isSelfOrDescendent(temporaryFolder.root, temporaryFolder.root)
    }

    def "returns true for files with same path"() {
        expect:
        FileSystemUtils.isSelfOrDescendent(temporaryFolder.root, new File(temporaryFolder.root.absolutePath))
    }

    def "returns true if file is decendent"() {
        expect:
        FileSystemUtils.isSelfOrDescendent(temporaryFolder.root, new File(temporaryFolder.root, 'foo'))
    }

    def "returns true if file paths starts the same but is not descendent"() {
        expect:
        !FileSystemUtils.isSelfOrDescendent(temporaryFolder.root, new File("${temporaryFolder.root.absolutePath}_foo"))
    }

    def "throws exception if self is null"() {
        when:
        FileSystemUtils.isSelfOrDescendent(null, temporaryFolder.root)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Could not check descendent because the specified file is null.'
    }

    def "throws exception if file to check is null"() {
        when:
        FileSystemUtils.isSelfOrDescendent(temporaryFolder.root, null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == 'Could not check descendent because the specified file is null.'
    }
}
