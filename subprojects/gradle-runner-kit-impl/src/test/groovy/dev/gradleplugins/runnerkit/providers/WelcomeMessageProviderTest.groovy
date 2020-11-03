package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.GradleWrapperFixture
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.GradleDistributionProvider
import dev.gradleplugins.runnerkit.providers.GradleUserHomeDirectoryProvider
import dev.gradleplugins.runnerkit.providers.WelcomeMessageProvider
import org.gradle.launcher.cli.DefaultCommandLineActionFactory
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.fixtures.file.FileSystemUtils.createFile
import static dev.gradleplugins.runnerkit.GradleExecutionContext.WelcomeMessage.DISABLED
import static dev.gradleplugins.runnerkit.GradleExecutionContext.WelcomeMessage.ENABLED
import static dev.gradleplugins.runnerkit.providers.WelcomeMessageProvider.disabled
import static dev.gradleplugins.runnerkit.providers.WelcomeMessageProvider.enabled

@Subject(WelcomeMessageProvider)
class WelcomeMessageProviderTest extends Specification implements FileSystemFixture, GradleWrapperFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "expect constant did not change"() {
        expect:
        WelcomeMessageProvider.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY == DefaultCommandLineActionFactory.WELCOME_MESSAGE_ENABLED_SYSTEM_PROPERTY
    }

    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.WelcomeMessage.values() as Set == [DISABLED, ENABLED] as Set
    }

    def "can provide hiding welcome message"() {
        expect:
        def subject = disabled()
        subject.isPresent()
        subject.get() == DISABLED
        subject.asJvmSystemProperties == ['org.gradle.internal.launcher.welcomeMessageEnabled': 'false']
        subject.asArguments == ['-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false']
    }

    def "can provide showing welcome message"() {
        expect:
        def subject = enabled()
        subject.isPresent()
        subject.get() == ENABLED
        subject.asJvmSystemProperties == ['org.gradle.internal.launcher.welcomeMessageEnabled': 'true']
        subject.asArguments == ['-Dorg.gradle.internal.launcher.welcomeMessageEnabled=true']
    }

    def "write magic file to disable welcome message"() {
        given:
        writeGradleWrapperToTestDirectory("6.5")
        def context = Stub(GradleExecutionContext) {
            getGradleUserHomeDirectory() >> GradleUserHomeDirectoryProvider.of(file('.gradle'))
            getDistribution() >> GradleDistributionProvider.fromGradleWrapper(testDirectory)
        }

        when:
        disabled().accept(context)

        then:
        file('.gradle/notifications/6.5/release-features.rendered').exists()
    }

    def "deletes magic file to disable welcome message"() {
        given:
        writeGradleWrapperToTestDirectory("6.5")
        def context = Stub(GradleExecutionContext) {
            getGradleUserHomeDirectory() >> GradleUserHomeDirectoryProvider.of(file('.gradle'))
            getDistribution() >> GradleDistributionProvider.fromGradleWrapper(testDirectory)
        }
        createFile(file('.gradle/notifications/6.5/release-features.rendered'))

        when:
        enabled().accept(context)

        then:
        !file('notifications/6.5/release-features.rendered').exists()
    }

    @Unroll
    def "throws exception when using command line flag"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of([flag])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == message

        where:
        provider    | flag                  | message
        enabled()   | '-Dorg.gradle.internal.launcher.welcomeMessageEnabled=true'   | 'Please remove command line flag enabling welcome message as the it was already enabled via GradleRunner#withWelcomeMessageEnabled().'
        enabled()   | '-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false'  | 'Please remove command line flag disabling welcome message and any call to GradleRunner#withWelcomeMessageEnabled() for this runner as it is disabled by default for all toolbox runner.'
        disabled()  | '-Dorg.gradle.internal.launcher.welcomeMessageEnabled=true'   | 'Please use GradleRunner#withWelcomeMessageEnabled() instead of using flag in command line arguments.'
        disabled()  | '-Dorg.gradle.internal.launcher.welcomeMessageEnabled=false'  | 'Please remove command line flag disabling welcome message as the it is disabled by default for all toolbox runner.'
    }

    def "does not throw exceptions when welcome message property is not in the command line arguments"() {
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.empty()
        }

        when:
        enabled().validate(context)
        then:
        noExceptionThrown()

        when:
        disabled().validate(context)
        then:
        noExceptionThrown()
    }
}
