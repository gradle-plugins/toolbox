package dev.gradleplugins.test.fixtures.gradle.executer

import dev.gradleplugins.test.fixtures.gradle.executer.internal.LogContent
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionResult
import org.hamcrest.Matchers
import spock.lang.Specification

import static org.hamcrest.Matchers.*

class OutputScrapingExecutionResultTest extends Specification {
    def "can assert mismatch in executed tasks"() {
        def result = new OutputScrapingExecutionResult(LogContent.of(outputUnderTest), LogContent.empty(), true)

        when:
        result.assertTasksExecuted(':skippedTask', ':cachedTask', ':upToDateTask', ':noSourceTask', ':executedTaskFoo')

        then:
        def exception = thrown(AssertionError)
        exception.message == '''Build output does not contain the expected tasks.
            |Expected: [:cachedTask, :executedTaskFoo, :noSourceTask, :skippedTask, :upToDateTask]
            |Actual: [:skippedTask, :cachedTask, :upToDataTask, :noSourceTask, :executedTaskFoo, :executedTaskBar]
            |Output:
            |=======
            |
            |> Task :skippedTask SKIPPED
            |> Task :cachedTask FROM-CACHE
            |> Task :upToDataTask UP-TO-DATE
            |> Task :noSourceTask NO-SOURCE
            |> Task :executedTaskFoo
            |> Task :executedTaskBar
            |A task not present in the output :notExecutedTask
            |
            |BUILD SUCCESSFUL in 40s
            |6 actionable tasks: 2 executed
            |
            |Error:
            |======
            |'''.stripMargin()
    }

    def "can match result output"() {
        def result = new OutputScrapingExecutionResult(LogContent.of(outputUnderTest), LogContent.empty(), true)

        when:
        result.assertThatOutput(containsString('> Task :skippedTask'))
        then:
        noExceptionThrown()

        when:
        result.assertThatOutput(containsString('no output'))
        then:
        def ex = thrown(AssertionError)
        ex.message == '''Output did not match!
            |Expected: a string containing "no output"
            |     but: was "
            |> Task :skippedTask SKIPPED
            |> Task :cachedTask FROM-CACHE
            |> Task :upToDataTask UP-TO-DATE
            |> Task :noSourceTask NO-SOURCE
            |> Task :executedTaskFoo
            |> Task :executedTaskBar
            |A task not present in the output :notExecutedTask
            |"'''.stripMargin()
    }

    protected String getOutputUnderTest() {
        return '''
            |> Task :skippedTask SKIPPED
            |> Task :cachedTask FROM-CACHE
            |> Task :upToDataTask UP-TO-DATE
            |> Task :noSourceTask NO-SOURCE
            |> Task :executedTaskFoo
            |> Task :executedTaskBar
            |A task not present in the output :notExecutedTask
            |
            |BUILD SUCCESSFUL in 40s
            |6 actionable tasks: 2 executed
            |'''.stripMargin()
    }
}
