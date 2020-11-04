package dev.gradleplugins.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.BuildFailure.describedBy

@Subject(BuildFailure)
class BuildFailureTest extends Specification {
    def "can create build failure from description"() {
        expect:
        def subject = describedBy('Some description')
        subject.description == 'Some description'
        subject.causes == []
    }

    def "can create new build failure with additional causes"() {
        expect:
        def subject = describedBy('Some description').causedBy('Some cause')
        subject.description == 'Some description'
        subject.causes == ['Some cause']

        and:
        subject.causedBy('Some other cause').causes == ['Some cause', 'Some other cause']
        subject.causes == ['Some cause']
    }

    def "can compare build failure"() {
        expect:
        describedBy('description') == describedBy('description')
        describedBy('description') != describedBy('other')
        describedBy('description') != describedBy('description').causedBy('cause')

        and:
        describedBy('description').causedBy('cause') == describedBy('description').causedBy('cause')
        describedBy('description').causedBy('cause') != describedBy('other').causedBy('cause')
        describedBy('description').causedBy('cause') != describedBy('description').causedBy('other')
        describedBy('description').causedBy('cause') != describedBy('description').causedBy('cause').causedBy('other')
    }
}
