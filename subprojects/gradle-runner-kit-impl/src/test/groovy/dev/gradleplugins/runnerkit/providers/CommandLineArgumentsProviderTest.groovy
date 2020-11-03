package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider.empty
import static dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider.of

@Subject(CommandLineArgumentsProvider)
class CommandLineArgumentsProviderTest extends Specification {
    def "can provide empty arguments"() {
        expect:
        def subject = empty()
        subject.isPresent()
        subject.get() == []
        subject.asArguments == []
    }

    def "can provide fixed arguments"() {
        expect:
        def subject = of(['a', 'b', 'c'])
        subject.isPresent()
        subject.get() == ['a', 'b', 'c']
        subject.asArguments == ['a', 'b', 'c']
    }

    def "can append arguments to empty"() {
        expect:
        def subject = empty().plus('a')
        subject.isPresent()
        subject.get() == ['a']
        subject.asArguments == ['a']
    }

    def "can append more arguments"() {
        expect:
        def subject = of(['a', 'b']).plus('c')
        subject.isPresent()
        subject.get() == ['a', 'b', 'c']
        subject.asArguments == ['a', 'b', 'c']
    }
}
