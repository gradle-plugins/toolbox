package dev.gradleplugins.test.fixtures.gradle.executer

import dev.gradleplugins.test.fixtures.gradle.executer.internal.LogContent
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionResult
import spock.lang.Specification

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
