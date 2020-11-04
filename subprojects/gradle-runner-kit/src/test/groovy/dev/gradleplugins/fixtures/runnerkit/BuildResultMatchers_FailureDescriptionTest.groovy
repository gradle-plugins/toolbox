package dev.gradleplugins.fixtures.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.*
import static dev.gradleplugins.runnerkit.BuildResult.from
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.not
import static spock.util.matcher.HamcrestSupport.expect

@Subject(BuildResultMatchers)
class BuildResultMatchers_FailureDescriptionTest extends Specification {
    def "can assert failure description starts with string"() {
        expect:
        expect from(output), hasFailureDescription("Execution failed for task ':foo'.")
        expect from(output), hasFailureDescription("Execution failed for task ':bar:foo'.")
        expect from(output), hasFailureDescription("Execution failed for task")

        and:
        expect from(output), not(hasFailureDescription("Unknown failure"))
    }

    def "can assert failure description using matcher"() {
        expect:
        expect from(output), hasFailureDescription(containsString(":foo"))
        expect from(output), hasFailureDescription(not(containsString(":bar:foo")))
    }

    def "fails when cannot assert failure description starts with string"() {
        when:
        assertThat from(output), hasFailureDescription("foo")

        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: a failure description matching a string starting with "foo"
            |     but: none of the following description matches: "Execution failed for task ':foo'.", "Execution failed for task ':bar:foo'.", "Execution failed for task ':foo:foo'.\"""".stripMargin()
    }

    def "fails when cannot assert failure description using matcher"() {
        when:
        assertThat from(output), hasFailureDescription(containsString("unknown"))
        then:
        def ex = thrown(AssertionError)
        ex.message == """
            |Expected: a failure description matching a string containing "unknown"
            |     but: none of the following description matches: "Execution failed for task ':foo'.", "Execution failed for task ':bar:foo'.", "Execution failed for task ':foo:foo'.\"""".stripMargin()
    }

    private static String getOutput() {
        return '''> Task :foo FAILED
            |> Task :foo:foo FAILED
            |> Task :bar:foo FAILED
            |
            |FAILURE: Build completed with 3 failures.
            |
            |1: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 15
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |==============================================================================
            |
            |2: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 54
            |
            |* What went wrong:
            |Execution failed for task ':bar:foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |==============================================================================
            |
            |3: Task failed with an exception.
            |-----------
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 44
            |
            |* What went wrong:
            |Execution failed for task ':foo:foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |==============================================================================
            |
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 827ms
            |3 actionable tasks: 3 executed'''.stripMargin()
    }
}
