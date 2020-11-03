package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.ConsoleTypeProvider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.ConsoleTypeProvider.defaultConsole
import static dev.gradleplugins.runnerkit.providers.ConsoleTypeProvider.richConsole
import static dev.gradleplugins.runnerkit.GradleExecutionContext.ConsoleType.RICH

@Subject(ConsoleTypeProvider)
class ConsoleTypeProviderTest extends Specification {
    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.ConsoleType.values() as Set == [RICH] as Set
    }

    def "can provider default console type"() {
        expect:
        !defaultConsole().isPresent()
        defaultConsole().asArguments == []
    }

    def "can provider rich console type"() {
        expect:
        def subject = richConsole()
        subject.isPresent()
        subject.get() == RICH
        subject.asArguments == ['--console', 'rich']
    }

    @Unroll
    def "fails if rich console flags is specified in the command line arguments"(flags) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(flags)
        }

        when:
        defaultConsole().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#withRichConsoleEnabled() instead of using the command line flags.'

        where:
        flags << [['--console', 'rich'], ['--console=rich']]
    }

    @Unroll
    def "fails if rich console flags is specified in the command line arguments when using rich console provider"(flags) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(flags)
        }

        when:
        richConsole().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please remove command line flags for rich console, rich console is already enabled.'

        where:
        flags << [['--console', 'rich'], ['--console=rich']]
    }

    @Unroll
    def "fails if other console flags when using other console command line flags"(flags, provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(flags)
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please open an issue on gradle-plugins/toolbox GitHub repository to support your use case.'

        where:
        [flags, provider] << [[['--console', 'verbose'], ['--console=verbose'], ['--console', 'auto'], ['--console=auto'], ['--console', 'plain'], ['--console=plain']], [defaultConsole(), richConsole()]].combinations()
    }

    def "can validate when no console flags as arguments"(provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.empty()
        }

        when:
        provider.validate(context)

        then:
        noExceptionThrown()

        where:
        provider << [defaultConsole(), richConsole()]
    }
}
