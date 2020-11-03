package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.file.FileSystemUtils
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.GradleUserHomeDirectoryProvider
import org.gradle.testkit.runner.internal.DefaultGradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.GradleUserHomeDirectoryProvider.of
import static dev.gradleplugins.runnerkit.providers.GradleUserHomeDirectoryProvider.testKitDirectory
import static org.apache.commons.lang3.SystemUtils.USER_NAME
import static org.apache.commons.lang3.SystemUtils.javaIoTmpDir

@Subject(GradleUserHomeDirectoryProvider)
class GradleUserHomeDirectoryProviderTest extends Specification implements FileSystemFixture{
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "expect constant did not change"() {
        expect:
        GradleUserHomeDirectoryProvider.TEST_KIT_DIR_SYS_PROP == DefaultGradleRunner.TEST_KIT_DIR_SYS_PROP
    }

    def "can provide default test kit directory"() {
        expect:
        def subject = testKitDirectory()
        subject.isPresent()
        subject.get() == FileSystemUtils.file(javaIoTmpDir, ".gradle-test-kit-${USER_NAME}")
        subject.asArguments == ['--gradle-user-home', FileSystemUtils.file(javaIoTmpDir, ".gradle-test-kit-${USER_NAME}").absolutePath]
    }

    def "can provide default test kit directory from System property"() {
        given:
        System.setProperty("org.gradle.testkit.dir", file('testKitDirectory').absolutePath)

        expect:
        def subject = testKitDirectory()
        subject.isPresent()
        subject.get() == file('testKitDirectory')
        subject.asArguments == ['--gradle-user-home', file('testKitDirectory').absolutePath]

        cleanup:
        System.clearProperty("org.gradle.testkit.dir")
    }

    def "can provide a custom Gradle user home directory"() {
        expect:
        def subject = of(file('some-dir'))
        subject.isPresent()
        subject.get() == file('some-dir')
        subject.asArguments == ['--gradle-user-home', file('some-dir').absolutePath]
    }

    def "can create a mapping relative to Gradle user home to be calculated later"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getGradleUserHomeDirectory() >> of(file('user-home'))
        }

        expect:
        def subject = GradleUserHomeDirectoryProvider.relativeToGradleUserHome('some-path')
        subject.apply(context) == file('user-home/some-path')
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
        ex.message == 'Please use GradleRunner#withUserHomeDirectory(File) instead of using the command line flags.'

        where:
        [flag, provider] << [['--gradle-user-home', '-g'], [testKitDirectory(), of(new File('foo'))]].combinations()
    }
}
