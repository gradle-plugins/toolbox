package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.DaemonIdleTimeoutProvider
import org.gradle.launcher.daemon.configuration.DaemonBuildOptions
import org.gradle.testkit.runner.internal.ToolingApiGradleExecutor
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.Duration

import static dev.gradleplugins.runnerkit.providers.DaemonIdleTimeoutProvider.of
import static dev.gradleplugins.runnerkit.providers.DaemonIdleTimeoutProvider.testKitIdleTimeout

@Subject(DaemonIdleTimeoutProvider)
class DaemonIdleTimeoutProviderTest extends Specification {
    def "expect constant did not change"() {
        expect:
        DaemonIdleTimeoutProvider.DAEMON_BUILD_OPTIONS_IDLE_TIMEOUT_OPTION_GRADLE_PROPERTY == DaemonBuildOptions.IdleTimeoutOption.GRADLE_PROPERTY
    }

    def "can use default Test Kit value"() {
        expect:
        def subject = testKitIdleTimeout()
        subject.isPresent()
        subject.get() == Duration.ofSeconds(120)
        subject.asJvmSystemProperties == ['org.gradle.daemon.idletimeout': '120000']
        subject.asArguments == ['-Dorg.gradle.daemon.idletimeout=120000']
    }

    def "can use any duration value"() {
        expect:
        def subject = of(Duration.ofDays(1))
        subject.isPresent()
        subject.get().toHours() == 24
        subject.asJvmSystemProperties == ['org.gradle.daemon.idletimeout': '86400000']
        subject.asArguments == ['-Dorg.gradle.daemon.idletimeout=86400000']
    }

    @Unroll
    def "throws exception when using system property flag in command line arguments"(provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(['-Dorg.gradle.daemon.idletimeout=120000'])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#withDaemonIdleTimeout(Duration) instead of using the command line flags.'

        where:
        provider << [of(Duration.ofHours(2)), testKitIdleTimeout()]
    }
}
