package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.ProjectDirectoryProvider
import dev.gradleplugins.runnerkit.providers.WorkingDirectoryProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.ProjectDirectoryProvider.of
import static dev.gradleplugins.runnerkit.providers.ProjectDirectoryProvider.useWorkingDirectoryImplicitly

@Subject(ProjectDirectoryProvider)
class ProjectDirectoryProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can provide project directory"() {
        expect:
        def subject = of(file('projectDir'))
        subject.isPresent()
        subject.get() == file('projectDir')
        subject.asArguments == ['--project-dir', file('projectDir').absolutePath]
    }

    def "can provide project directory as working directory implicitly"() {
        expect:
        def subject = useWorkingDirectoryImplicitly()
        !subject.isPresent()
        subject.asArguments == []
    }

    def "throws exception if working directory is not set when using it implicitly"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getWorkingDirectory() >> WorkingDirectoryProvider.unset()
        }

        when:
        useWorkingDirectoryImplicitly().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please specify a working directory via GradleRunner#inDirectory(File) or a project directory via GradleRunner#usingProjectDirectory(File).'
    }

    def "does not throw exception if working directory is set when using it implicitly"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getWorkingDirectory() >> WorkingDirectoryProvider.of(testDirectory)
        }

        when:
        useWorkingDirectoryImplicitly().validate(context)

        then:
        noExceptionThrown()
    }

    def "does not throw exception when using explicit project directory regardless of presence of working directory"() {
        given:
        def context = Stub(GradleExecutionContext)

        when:
        of(testDirectory).validate(context)
        then:
        noExceptionThrown()

        when:
        of(testDirectory).validate(context)
        then:
        noExceptionThrown()
    }

    @Unroll
    def "throws exception when using project directory flag in command line arguments"(flag, provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getWorkingDirectory() >> WorkingDirectoryProvider.of(testDirectory)
            getArguments() >> CommandLineArgumentsProvider.of([flag, file('dir').absolutePath])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#usingProjectDirectory(File) instead of using the command line flags.'

        where:
        [flag, provider] << [['--project-dir', '-p'], [useWorkingDirectoryImplicitly(), of(new File('foo'))]].combinations()
    }
}
