package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.BuildScriptProvider
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.BuildScriptProvider.of
import static dev.gradleplugins.runnerkit.providers.BuildScriptProvider.unset

@Subject(BuildScriptProvider)
class BuildScriptProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "unset provider has no value"() {
        expect:
        !unset().isPresent()
        unset().asArguments == []
    }

    def "set provider has execution flags"() {
        expect:
        def subject = of(file('build.gradle'))
        subject.isPresent()
        subject.get() == file('build.gradle')
        subject.asArguments == ['--build-file', file('build.gradle').absolutePath]
    }

    @Unroll
    def "throws exception when using build file flag in command line arguments"(flag, provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of([flag, file('build.gradle').absolutePath])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#usingBuildScript(File) instead of using the command line flags.'

        where:
        [flag, provider] << [['--build-file', '-b'], [unset(), of(new File('foo'))]].combinations()
    }
}
