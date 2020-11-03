package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.providers.GradleTasksProvider
import spock.lang.Specification
import spock.lang.Subject

@Subject(GradleTasksProvider)
class GradleTasksProviderTest extends Specification {
    def "can provide empty task list"() {
        expect:
        def subject = GradleTasksProvider.empty()
        subject.isPresent()
        subject.get() == []
        subject.asArguments == []
    }

    def "can add task to an empty task list"() {
        expect:
        def subject = GradleTasksProvider.empty().plus(['a', 'b'])
        subject.isPresent()
        subject.get() == ['a', 'b']
        subject.asArguments == ['a', 'b']
    }

    def "can provide task list"() {
        expect:
        def subject = GradleTasksProvider.of(['a', 'b'])
        subject.isPresent()
        subject.get() == ['a', 'b']
        subject.asArguments == ['a', 'b']
    }
}
