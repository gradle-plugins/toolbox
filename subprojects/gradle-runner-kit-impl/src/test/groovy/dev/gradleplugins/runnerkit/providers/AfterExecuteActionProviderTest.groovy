package dev.gradleplugins.runnerkit.providers

import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer

import static dev.gradleplugins.runnerkit.providers.AfterExecuteActionsProvider.empty

@Subject(AfterExecuteActionsProvider)
class AfterExecuteActionProviderTest extends Specification {
    def "can create empty provider"() {
        expect:
        def subject = empty()
        subject.isPresent()
        subject.get() == []
    }

    def "can add more elements"() {
        given:
        def action = Stub(Consumer)
        expect:
        def subject = empty().plus(action)
        subject.isPresent()
        subject.get() == [action]
    }
}
