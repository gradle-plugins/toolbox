package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(InjectedClasspathProvider)
class InjectedClasspathProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can create empty injected classpath"() {
        expect:
        def subject = InjectedClasspathProvider.empty()
        subject.isPresent()
        subject.get() == []
    }

    def "can create injected classpath from list of files"() {
        given:
        def entry1 = file('foo')
        def entry2 = file('bar.jar')
        def entry3 = file('far.jar')

        expect:
        def subject = InjectedClasspathProvider.of([entry1, entry2, entry3])
        subject.isPresent()
        subject.get() == [entry1, entry2, entry3]
    }

    def "can create injected classpath from metadata properties"() {
        expect:
        def subject = InjectedClasspathProvider.fromPluginUnderTestMetadata()
        subject.isPresent()
        subject.get()*.absolutePath.every { it.endsWith('/build/classes/java/main') || it.endsWith('/foo.jar') || it.endsWith('/bar.jar')}
    }
}
