package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.SettingsFileProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.SettingsFileProvider.of
import static dev.gradleplugins.runnerkit.providers.SettingsFileProvider.unset

@Subject(SettingsFileProvider)
class SettingsFileProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can provide unset settings file"() {
        expect:
        def subject = unset()
        !subject.isPresent()
        subject.asArguments == []
    }

    def "can provide a settings file"() {
        expect:
        def subject = of(file('foo.gradle'))
        subject.isPresent()
        subject.get() == file('foo.gradle')
        subject.asArguments == ['--settings-file', file('foo.gradle').absolutePath]
    }

    @Unroll
    def "throws exception when using settings file flag in command line arguments"(flag, provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of([flag, file('foo').absolutePath])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#usingSettingsFile(File) instead of using the command line flags.'

        where:
        [flag, provider] << [['--settings-file', '-c'], [unset(), of(new File('foo'))]].combinations()
    }
}
