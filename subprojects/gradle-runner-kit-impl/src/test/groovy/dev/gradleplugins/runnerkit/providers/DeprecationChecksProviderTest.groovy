package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.DeprecationChecksProvider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.DeprecationChecksProvider.fails
import static dev.gradleplugins.runnerkit.providers.DeprecationChecksProvider.ignores
import static dev.gradleplugins.runnerkit.GradleExecutionContext.DeprecationChecks.FAILS

@Subject(DeprecationChecksProvider)
class DeprecationChecksProviderTest extends Specification {
    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.DeprecationChecks.values() as Set == [FAILS] as Set
    }

    def "can provide failing deprecation checks"() {
        expect:
        def subject = fails()
        subject.isPresent()
        subject.get() == FAILS
        subject.asArguments == ['--warning-mode', 'fail']
    }

    def "can provide ignoring deprecation checks"() {
        expect:
        def subject = ignores()
        !subject.isPresent()
        subject.asArguments == []
    }

    @Unroll
    def "throws exception when using warning mode flag in command line arguments"(provider, mode, message) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(['--warning-mode', mode])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == message

        where:
        provider    | mode      | message
        fails()     | 'fails'   | 'Please remove command line flag for failing warning mode as it is the default for all toolbox runner.'
        fails()     | 'all'     | 'Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.'
        fails()     | 'summary' | 'Please use GradleRunner#withoutDeprecationChecks() instead of using the command line flags.'
        fails()     | 'none'    | 'Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.'

        ignores()   | 'fails'   | 'Please remove command line flag for failing warning mode and any calls to GradleRunner#withoutDeprecationChecks() for this runner as it is the default for all toolbox runner.'
        ignores()   | 'all'     | 'Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.'
        ignores()   | 'summary' | 'Please remove command line flag for summary warning mode as GradleRunner#withoutDeprecationChecks() already configure summary warning mode.'
        ignores()   | 'none'    | 'Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.'
    }
}
