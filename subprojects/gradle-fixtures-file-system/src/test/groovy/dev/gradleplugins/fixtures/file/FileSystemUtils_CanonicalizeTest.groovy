package dev.gradleplugins.fixtures.file

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(FileSystemUtils)
class FileSystemUtils_CanonicalizeTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "throws uncheck exception if file canonicalization fails"() {
        given:
        def file = new ThrowingCanonicalFile(temporaryFolder.root.toString())

        when:
        FileSystemUtils.canonicalize(file)

        then:
        def ex = thrown(UncheckedIOException)
        ex.message == "Could not canonicalize '${temporaryFolder.root}'."
    }

    def "returns canonical file"() {
        when:
        def result = FileSystemUtils.canonicalize(temporaryFolder.root)

        then:
        result instanceof File
        result.absolutePath == temporaryFolder.root.canonicalPath
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
