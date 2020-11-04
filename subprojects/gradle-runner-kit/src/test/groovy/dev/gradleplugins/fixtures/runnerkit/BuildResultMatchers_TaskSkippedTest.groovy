package dev.gradleplugins.fixtures.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksSkipped
import static dev.gradleplugins.runnerkit.BuildResult.from
import static org.hamcrest.MatcherAssert.assertThat
import static spock.util.matcher.HamcrestSupport.expect

@Subject(BuildResultMatchers)
class BuildResultMatchers_TaskSkippedTest extends Specification {
    def "can assert skipped tasks in order"() {
        expect:
        expect from(output), tasksSkipped(':b', ':c', ':foo:b', ':foo:c')
    }

    def "can assert skipped tasks in any order"() {
        expect:
        expect from(output), tasksSkipped(':foo:b', ':foo:c', ':b', ':c')
    }

    def "can assert skipped tasks with nested iterable"() {
        expect:
        expect from(output), tasksSkipped([':foo:b', ':foo:c', ':b', ':c'])
        expect from(output), tasksSkipped([':foo:b', ':foo:c'], ':b', ':c')
        expect from(output), tasksSkipped([':foo:b', [':foo:c']], ':b', [':c'])
    }

    def "fails if more tasks are skipped then asserting"() {
        when:
        assertThat(from(output), tasksSkipped(':b', ':c'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: skipped task paths of iterable with items [":b", ":c"] in any order
            |     but: no match for: ":foo:b\"""".stripMargin()
    }

    def "fails if less tasks are skipped then asserting"() {
        when:
        assertThat(from(output), tasksSkipped(':b', ':c', ':d', ':foo:b', ':foo:c'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: skipped task paths of iterable with items [":b", ":c", ":d", ":foo:b", ":foo:c"] in any order
            |     but: no item matches: ":d" in [":b", ":c", ":foo:b", ":foo:c"]""".stripMargin()
    }

    def "fails if expected task paths does not starts with ':'"() {
        when:
        assertThat(from(output), tasksSkipped('b', ':c', ':foo:b', ':foo:c'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """all expected task paths must be valid task paths
            |Expected: every item is a string starting with ":"
            |     but: an item was "b\"""".stripMargin()
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
