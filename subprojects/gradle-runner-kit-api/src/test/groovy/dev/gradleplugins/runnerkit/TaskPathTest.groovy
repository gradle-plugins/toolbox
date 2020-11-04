package dev.gradleplugins.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.TaskPath.of

@Subject(TaskPath)
class TaskPathTest extends Specification {
    def "can query the project path"() {
        expect:
        of(':foo').projectPath == ':'
        of(':buildSrc:foo').projectPath == ':buildSrc'
        of(':a:b:foo').projectPath == ':a:b'
    }

    def "can query the task name"() {
        expect:
        of(':a').taskName == 'a'
        of(':buildSrc:b').taskName == 'b'
        of(':a:b:c').taskName == 'c'
    }

    def "can get the task path"() {
        expect:
        of(':a').get() == ':a'
        of(':buildSrc:b').get() == ':buildSrc:b'
        of(':a:b:c').get() == ':a:b:c'
    }

    def "toString returns the task path"() {
        expect:
        of(':a').toString() == ':a'
        of(':buildSrc:b').toString() == ':buildSrc:b'
        of(':a:b:c').toString() == ':a:b:c'
    }

    def "can compare task paths"() {
        expect:
        of(':a') == of(':a')
        of(':foo:a') == of(':foo:a')
        of(':a') != of(':foo:a')
        of(':a') != of('b')
    }
}
