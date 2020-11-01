package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_FileTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "returns canonical root file when path segment"() {
        when:
        def result = FileSystemUtils.file(temporaryFolder.root)
        
        then:
        result instanceof File
        result.absolutePath == temporaryFolder.root.canonicalPath
    }

    def "joins path segment using toString only"() {
        given:
        def segment = Mock(Object)

        when:
        def result = FileSystemUtils.file(temporaryFolder.root, 'string', segment)

        then:
        1 * segment.toString() >> 'object'

        and:
        result.absolutePath == "${temporaryFolder.root.canonicalPath}/string/object"
    }

    def "throws exception if toString throws an exception with the cause"() {
        given:
        def segment = Mock(Object)

        when:
        FileSystemUtils.file(temporaryFolder.root, 'string', segment)

        then:
        1 * segment.toString() >> { throw new RuntimeException('error') }

        and:
        def ex = thrown(RuntimeException)
        ex.message == 'Could not stringify path object #1.'
        ex.cause instanceof RuntimeException
        ex.cause.message == 'error'
    }

    def "throws exception if toString returns null"() {
        when:
        FileSystemUtils.file(temporaryFolder.root, 'string', new NullReturningToStringObject())

        then:
        def ex = thrown(RuntimeException)
        ex.message == 'Could not stringify path object #1.'
        ex.cause instanceof NullPointerException
        ex.cause.message == 'NullReturningToStringObject#toString() returns null.'
    }

    private static final class NullReturningToStringObject extends Object {
        @Override
        public String toString() {
            return null
        }
    }

    def "throws uncheck exception if file canonicalization fails"() {
        given:
        def file = new ThrowingCanonicalFile(temporaryFolder.root.toString())

        when:
        FileSystemUtils.file(file)

        then:
        def ex = thrown(UncheckedIOException)
        ex.message == "Could not canonicalize '${temporaryFolder.root}'."
    }

    def "throws exception if root file is null"() {
        when:
        FileSystemUtils.file(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not join file because the specified file is null."
    }

    def "throws exception any path object is null"() {
        when:
        FileSystemUtils.file(temporaryFolder.root, 'path', null);

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Could not join file because path object #1 is null."
    }

    private static final class ThrowingCanonicalFile extends File {
        ThrowingCanonicalFile(String pathname) {
            super(pathname)
        }

        @Override
        File getCanonicalFile() throws IOException {
            throw new IOException()
        }

        @Override
        File getAbsoluteFile() {
            return this;
        }
    }
}
