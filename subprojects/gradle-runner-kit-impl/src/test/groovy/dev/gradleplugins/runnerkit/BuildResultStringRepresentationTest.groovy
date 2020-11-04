package dev.gradleplugins.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.BuildResult.from

@Subject(BuildResultImpl)
class BuildResultStringRepresentationTest extends Specification {
    def "list all build task with outcome"() {
        expect:
        from(simpleOutput).toString() == '''> Task :foo
            |> Task :bar:foo UP-TO-DATE
            |
            |BUILD SUCCESSFUL
            |2 actionable tasks: 1 executed, 1 up-to-date'''.stripMargin()
    }

    def "list all build task with outcome and single failure"() {
        expect:
        from(simpleFailingOutput).toString() == '''> Task :foo FAILED
            |
            |FAILURE: Build failed with an exception.
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |BUILD FAILED
            |1 actionable task: 1 executed'''.stripMargin()
    }

    def "list all build task with outcome and multiple failures"() {
        expect:
        from(multiFailingOutput).toString() == '''> Task :foo FAILED
            |> Task :bar:foo FAILED
            |> Task :foo:foo FAILED
            |
            |FAILURE: Build completed with 3 failures.
            |
            |1: Task failed with an exception.
            |-----------
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |==============================================================================
            |
            |2: Task failed with an exception.
            |-----------
            |* What went wrong:
            |Execution failed for task ':foo:foo'.
            |> Fail to execute
            |==============================================================================
            |
            |3: Task failed with an exception.
            |-----------
            |* What went wrong:
            |Execution failed for task ':bar:foo'.
            |> Fail to execute
            |==============================================================================
            |
            |BUILD FAILED
            |3 actionable tasks: 3 executed'''.stripMargin()
    }

    private String getSimpleOutput() {
        return '''> Task :foo
            |> Task :bar:foo UP-TO-DATE
            |
            |BUILD SUCCESSFUL in 490ms
            |2 actionable task: 1 executed, 1 up-to-date'''.stripMargin()
    }

    private static String getSimpleFailingOutput() {
        return '''> Task :foo FAILED
            |
            |FAILURE: Build failed with an exception.
            |
            |* Where:
            |Build file '/Users/daniel/gradle/tmp/build-result-test/build.gradle' line: 10
            |
            |* What went wrong:
            |Execution failed for task ':foo'.
            |> Fail to execute
            |
            |* Try:
            |Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
            |
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 488ms
            |1 actionable task: 1 executed'''.stripMargin()
    }

    private static String getMultiFailingOutput() {
        return '''> Task :foo FAILED
            |> Task :bar:foo FAILED
            |> Task :foo:foo FAILED
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
            |3: Task failed with an exception.
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
            |* Get more help at https://help.gradle.org
            |
            |BUILD FAILED in 488ms
            |3 actionable tasks: 3 executed'''.stripMargin()
    }

    def "does not normalize actionable task count for output with buildSrc"() {
        expect:
        from(outputWithBuildSrc).toString() == '''> Task :buildSrc:compileJava NO-SOURCE
            |> Task :buildSrc:compileGroovy NO-SOURCE
            |> Task :buildSrc:processResources NO-SOURCE
            |> Task :buildSrc:classes UP-TO-DATE
            |> Task :buildSrc:jar
            |> Task :buildSrc:assemble
            |> Task :buildSrc:compileTestJava NO-SOURCE
            |> Task :buildSrc:compileTestGroovy NO-SOURCE
            |> Task :buildSrc:processTestResources NO-SOURCE
            |> Task :buildSrc:testClasses UP-TO-DATE
            |> Task :buildSrc:test NO-SOURCE
            |> Task :buildSrc:check UP-TO-DATE
            |> Task :buildSrc:build
            |> Task :foo
            |
            |BUILD SUCCESSFUL
            |1 actionable task: 1 executed'''.stripMargin()
    }

    def "can ignore buildSrc from result"() {
        expect:
        from(outputWithBuildSrc).withoutBuildSrc().toString() == '''> Task :foo
            |
            |BUILD SUCCESSFUL
            |1 actionable task: 1 executed'''.stripMargin()
    }

    private static String getOutputWithBuildSrc() {
        return '''> Task :buildSrc:compileJava NO-SOURCE
            |> Task :buildSrc:compileGroovy NO-SOURCE
            |> Task :buildSrc:processResources NO-SOURCE
            |> Task :buildSrc:classes UP-TO-DATE
            |> Task :buildSrc:jar
            |> Task :buildSrc:assemble
            |> Task :buildSrc:compileTestJava NO-SOURCE
            |> Task :buildSrc:compileTestGroovy NO-SOURCE
            |> Task :buildSrc:processTestResources NO-SOURCE
            |> Task :buildSrc:testClasses UP-TO-DATE
            |> Task :buildSrc:test NO-SOURCE
            |> Task :buildSrc:check UP-TO-DATE
            |> Task :buildSrc:build
            |> Task :foo
            |
            |BUILD SUCCESSFUL in 3s
            |1 actionable task: 1 executed'''.stripMargin()
    }
}
