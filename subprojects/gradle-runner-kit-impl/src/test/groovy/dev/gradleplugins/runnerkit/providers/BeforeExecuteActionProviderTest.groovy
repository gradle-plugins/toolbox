package dev.gradleplugins.runnerkit.providers

import spock.lang.Specification
import spock.lang.Subject

import java.util.function.UnaryOperator

import static dev.gradleplugins.runnerkit.providers.BeforeExecuteActionsProvider.empty

@Subject(BeforeExecuteActionsProvider)
class BeforeExecuteActionProviderTest extends Specification {
    def "can create empty provider"() {
        expect:
        def subject = empty()
        subject.isPresent()
        subject.get() == []
    }

    def "can add more elements"() {
        given:
        def action = Stub(UnaryOperator)
        expect:
        def subject = empty().plus(action)
        subject.isPresent()
        subject.get() == [action]
    }
}
