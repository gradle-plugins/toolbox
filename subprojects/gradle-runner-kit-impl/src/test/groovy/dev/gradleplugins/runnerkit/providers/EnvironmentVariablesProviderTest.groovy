package dev.gradleplugins.runnerkit.providers

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.providers.EnvironmentVariablesProvider.inherited
import static dev.gradleplugins.runnerkit.providers.EnvironmentVariablesProvider.of

@Subject(EnvironmentVariablesProvider)
class EnvironmentVariablesProviderTest extends Specification {
    def "can use inherited value"() {
        expect:
        def subject = inherited()
        !subject.isPresent() // not present, it's up to the context to decide
    }

    def "can use specific values"() {
        expect:
        def subject = of([A: 'a', B: 'b'])
        subject.isPresent()
        subject.get() == [A: 'a', B: 'b']
    }

    def "can add environment variables to the inherited values"() {
        expect:
        def subject = inherited().plus([A:'a'])
        subject.isPresent()
        subject.get() == System.getenv() + [A: 'a']
    }

    def "can add environment variables to the specific values"() {
        expect:
        def subject = of([A:'a']).plus(B: 'b', C: 'c')
        subject.isPresent()
        subject.get() == [A: 'a', B: 'b', C: 'c']
    }

    def "can merge environment variables to the specific values"() {
        expect:
        def subject = of([A:'a']).plus(A: 'aa').plus(A: 'aaa')
        subject.isPresent()
        subject.get() == [A: 'aaa']
    }
}
