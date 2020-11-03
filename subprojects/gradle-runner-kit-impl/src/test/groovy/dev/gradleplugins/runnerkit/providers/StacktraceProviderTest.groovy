package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.StacktraceProvider
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.GradleExecutionContext.Stacktrace.HIDE
import static dev.gradleplugins.runnerkit.GradleExecutionContext.Stacktrace.SHOW
import static dev.gradleplugins.runnerkit.providers.StacktraceProvider.hide
import static dev.gradleplugins.runnerkit.providers.StacktraceProvider.show

@Subject(StacktraceProvider)
class StacktraceProviderTest extends Specification {
    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.Stacktrace.values() as Set == [HIDE, SHOW] as Set
    }


    def "can provide showing stacktrace"() {
        expect:
        def subject = show()
        subject.isPresent()
        subject.get() == SHOW
        subject.asArguments == ['--stacktrace']
    }

    def "can provide hiding stacktrace"() {
        expect:
        def subject = hide()
        subject.isPresent()
        subject.get() == HIDE
        subject.asArguments == []
    }

    def "throws exception when passing the stacktrace command line flags when already showing"(flag) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of([flag])
        }

        when:
        show().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please remove stacktrace command line flags as showing the stacktrace is the default behavior of all toolbox runner.'

        where:
        flag << ['--stacktrace', '-s']
    }
}
