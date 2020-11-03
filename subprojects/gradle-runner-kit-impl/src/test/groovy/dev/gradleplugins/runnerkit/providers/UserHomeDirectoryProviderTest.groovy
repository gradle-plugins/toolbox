package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.UserHomeDirectoryProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.UserHomeDirectoryProvider.implicit
import static dev.gradleplugins.runnerkit.providers.UserHomeDirectoryProvider.of

@Subject(UserHomeDirectoryProvider)
class UserHomeDirectoryProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can provide implicitly declared user home directory"() {
        expect:
        def subject = implicit()
        !subject.isPresent()
        subject.asJvmSystemProperties == [:]
        subject.asArguments == []
    }

    def "can provide explicitly declared user home directory"() {
        expect:
        def subject = of(file('user-home'))
        subject.isPresent()
        subject.get() == file('user-home')
        subject.asJvmSystemProperties == ['user.home': file('user-home').absolutePath]
        subject.asArguments == ["-Duser.home=${file('user-home').absolutePath}"]*.toString()
    }

    @Unroll
    def "throws exception when using system property flag in command line arguments"(provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(['-Duser.home', file('dir').absolutePath])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#withUserHomeDirectory(File) instead of using the command line flags.'

        where:
        provider << [implicit(), of(new File('foo'))]
    }
}
