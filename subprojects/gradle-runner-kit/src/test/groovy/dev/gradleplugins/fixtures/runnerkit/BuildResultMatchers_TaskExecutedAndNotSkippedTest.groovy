package dev.gradleplugins.fixtures.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.tasksExecutedAndNotSkipped
import static dev.gradleplugins.runnerkit.BuildResult.from
import static org.hamcrest.MatcherAssert.assertThat
import static spock.util.matcher.HamcrestSupport.expect

@Subject(BuildResultMatchers)
class BuildResultMatchers_TaskExecutedAndNotSkippedTest extends Specification {
    def "can assert executed and not skipped tasks in order"() {
        expect:
        expect from(output), tasksExecutedAndNotSkipped(':a', ':foo:a', ':bar:a', ':bar:b')
    }

    def "can assert executed and not skipped tasks in any order"() {
        expect:
        expect from(output), tasksExecutedAndNotSkipped(':bar:a', ':bar:b', ':a', ':foo:a')
    }

    def "can assert executed and not skipped tasks with nested iterable"() {
        expect:
        expect from(output), tasksExecutedAndNotSkipped([':bar:a', ':bar:b', ':a', ':foo:a'])
        expect from(output), tasksExecutedAndNotSkipped([':bar:a', ':bar:b'], ':a', ':foo:a')
        expect from(output), tasksExecutedAndNotSkipped([':bar:a', [':bar:b']], ':a', ':foo:a')
    }

    def "fails if more tasks are executed and not skipped then asserting"() {
        when:
        assertThat(from(output), tasksExecutedAndNotSkipped(':a', ':foo:a', ':bar:a'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: executed and not skipped task paths of iterable with items [":a", ":foo:a", ":bar:a"] in any order
            |     but: no match for: ":bar:b\"""".stripMargin()
    }

    def "fails if less tasks are executed and not skipped then asserting"() {
        when:
        assertThat(from(output), tasksExecutedAndNotSkipped(':a', ':b', ':foo:a', ':bar:a', ':bar:b'))

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: executed and not skipped task paths of iterable with items [":a", ":b", ":foo:a", ":bar:a", ":bar:b"] in any order
            |     but: no item matches: ":b" in [":a", ":foo:a", ":bar:a", ":bar:b"]""".stripMargin()
    }

    def "fails if expected task paths does not starts with ':'"() {
        when:
        assertThat(from(output), tasksExecutedAndNotSkipped('a', ':foo:a', ':bar:a', ':bar:b'))

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
