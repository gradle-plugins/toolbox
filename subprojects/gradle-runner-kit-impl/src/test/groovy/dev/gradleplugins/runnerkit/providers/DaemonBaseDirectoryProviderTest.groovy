package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.DaemonBaseDirectoryProvider
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions
import org.gradle.testkit.runner.internal.ToolingApiGradleExecutor
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.DaemonBaseDirectoryProvider.of

@Subject(DaemonBaseDirectoryProvider)
class DaemonBaseDirectoryProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "expect constant did not change"() {
        expect:
        DaemonBaseDirectoryProvider.TEST_KIT_DAEMON_DIR_NAME == ToolingApiGradleExecutor.TEST_KIT_DAEMON_DIR_NAME
        DaemonBaseDirectoryProvider.DAEMON_BUILD_OPTIONS_BASE_DIR_GRADLE_PROPERTY == DaemonBuildOptions.BaseDirOption.GRADLE_PROPERTY
    }

    def "can specify custom daemon base directory"() {
        expect:
        def subject = of(file('daemon'))
        subject.isPresent()
        subject.get() == file('daemon')
        subject.asJvmSystemProperties == ['org.gradle.daemon.registry.base': file('daemon').absolutePath]
        subject.asArguments == ["-Dorg.gradle.daemon.registry.base=${file('daemon').absolutePath}"]*.toString()
    }

    def "can use calculated directory"() {
        expect:
        def subject = of({file('daemon') })
        subject.isPresent()

        and:
        subject.calculateValue(Stub(GradleExecutionContext))
        subject.get() == file('daemon')
        subject.asJvmSystemProperties == ['org.gradle.daemon.registry.base': file('daemon').absolutePath]
        subject.asArguments == ["-Dorg.gradle.daemon.registry.base=${file('daemon').absolutePath}"]*.toString()
    }

    @Unroll
    def "throws exception when using system property flag in command line arguments"(provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(['-Dorg.gradle.daemon.registry.base=/foo/bar'])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#withDaemonBaseDirectory(File) instead of using the command line flags.'

        where:
        provider << [of(new File('daemon')), of({return new File('daemon')})]
    }
}
