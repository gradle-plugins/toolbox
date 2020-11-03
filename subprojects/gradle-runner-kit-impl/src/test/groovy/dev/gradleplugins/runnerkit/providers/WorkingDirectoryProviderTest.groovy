package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.providers.WorkingDirectoryProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(WorkingDirectoryProvider)
class WorkingDirectoryProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can provide unset working directory"() {
        expect:
        def subject = WorkingDirectoryProvider.unset()
        !subject.isPresent()
    }

    def "can provide working directory"() {
        expect:
        def subject = WorkingDirectoryProvider.of(file('working-dir'))
        subject.isPresent()
        subject.get() == file('working-dir')
    }
}
