package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.InitScriptsProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.InitScriptsProvider.empty

@Subject(InitScriptsProvider)
class InitScriptsProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can provide an empty list of init scripts"() {
        expect:
        def subject = empty()
        subject.isPresent()
        subject.get() == []
        subject.asArguments == []
    }

    def "can add init script to empty list"() {
        expect:
        def subject = empty().plus(file('a.init.gradle'))
        subject.isPresent()
        subject.get() == [file('a.init.gradle')]
        subject.asArguments == ['--init-script', file('a.init.gradle').absolutePath]
    }

    @Unroll
    def "throws exception when using init script command line flags"(flags, provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(flags)
        }
        when:
        empty().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#usingInitScript(File) instead of using the command line flags.'

        where:
        [flags, provider] << [[['--init-script', 'foo'], ['-I', 'foo']], [empty(), empty().plus(new File('foo'))]].combinations()
    }
}
