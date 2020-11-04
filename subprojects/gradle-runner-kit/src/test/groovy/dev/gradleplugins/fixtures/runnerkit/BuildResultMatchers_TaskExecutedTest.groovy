package dev.gradleplugins.fixtures.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.*
import static dev.gradleplugins.runnerkit.BuildResult.from
import static org.hamcrest.MatcherAssert.assertThat
import static spock.util.matcher.HamcrestSupport.expect

@Subject(BuildResultMatchers)
class BuildResultMatchers_TaskExecutedTest extends Specification {
    def "can assert executed tasks in order"() {
        expect:
        expect from(output), tasksExecuted(':a', ':b', ':c', ':foo:a', ':foo:b', ':foo:c', ':bar:a', ':bar:b')
    }

    def "can assert executed tasks in any order"() {
        expect:
        expect from(output), tasksExecuted(':bar:a', ':bar:b', ':a', ':foo:a', ':b', ':c', ':foo:b', ':foo:c')
    }

    def "can assert executed tasks with nested iterable"() {
        expect:
        expect from(output), tasksExecuted([':bar:a', ':bar:b', ':a', ':foo:a', ':b', ':c', ':foo:b', ':foo:c'])
        expect from(output), tasksExecuted([':bar:a', ':bar:b'], ':a', ':foo:a', ':b', ':c', ':foo:b', ':foo:c')
        expect from(output), tasksExecuted([':bar:a', [':bar:b']], ':a', [':foo:a'], ':b', ':c', ':foo:b', ':foo:c')
    }

    def "fails if more tasks are executed then asserting"() {
        when:
        assertThat(from(output), tasksExecuted(':a', ':b', ':c'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: executed task paths of iterable with items [":a", ":b", ":c"] in any order
            |     but: no match for: ":foo:a\"""".stripMargin()
    }

    def "fails if less tasks are executed then asserting"() {
        when:
        assertThat(from(output), tasksExecuted(':a', ':b', ':c', ':d', ':foo:a', ':foo:b', ':foo:c', ':bar:a', ':bar:b'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: executed task paths of iterable with items [":a", ":b", ":c", ":d", ":foo:a", ":foo:b", ":foo:c", ":bar:a", ":bar:b"] in any order
            |     but: no item matches: ":d" in [":a", ":b", ":c", ":foo:a", ":foo:b", ":foo:c", ":bar:a", ":bar:b"]""".stripMargin()
    }

    def "fails if expected task paths does not starts with ':'"() {
        when:
        assertThat(from(output), tasksExecuted('a', ':b', ':c', 'foo:a', ':foo:b', ':foo:c', ':bar:a', ':bar:b'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """all expected task paths must be valid task paths
            |Expected: every item is a string starting with ":"
            |     but: an item was "a\"""".stripMargin()
    }

    private static String getOutput() {
        return '''> Task :a
            |> Task :b SKIPPED
            |> Task :c NO-SOURCE
            |> Task :foo:a
            |> Task :foo:b UP-TO-DATE
            |> Task :foo:c FROM-CACHE
            |> Task :bar:a
            |> Task :bar:b
            |
            |BUILD SUCCESSFUL
            |8 actionable tasks: 4 executed, 4 up-to-date'''.stripMargin()
    }
}
