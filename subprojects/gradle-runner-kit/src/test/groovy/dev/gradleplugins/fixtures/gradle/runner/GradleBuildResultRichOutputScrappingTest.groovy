package dev.gradleplugins.fixtures.gradle.runner

import dev.nokee.core.exec.CommandLineToolLogContent
import spock.lang.Specification

import static dev.gradleplugins.fixtures.gradle.runner.GradleBuildResultImpl.from
import static dev.nokee.core.exec.CommandLineToolLogContent.of

class GradleBuildResultRichOutputScrappingTest extends Specification {
    def "two rich output of different amount of task executed are not equals"() {
        def output1 = '''
            |BUILD SUCCESSFUL in 441ms
            |1 actionable task: 1 executed'''.stripMargin()
        def output2 = '''
            |BUILD SUCCESSFUL in 501ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(of(output1)) != from(of(output2))
    }

    def "can downgrade verbose output build result to match rich output build result"() {
        def outputVerbose = '''> Task :foo
            |
            |BUILD SUCCESSFUL in 490ms
            |1 actionable task: 1 executed'''.stripMargin()
        def outputRich = '''
            |BUILD SUCCESSFUL in 441ms
            |1 actionable task: 1 executed'''.stripMargin()

        expect:
        from(of(outputVerbose)).asRichOutputResult() == from(of(outputRich))
        from(of(outputVerbose)).asRichOutputResult().executedTaskPaths == []
        from(of(outputVerbose)).asRichOutputResult().skippedTaskPaths == []
        from(of(outputVerbose)).asRichOutputResult().task(':foo') == null
    }

    def "end of logs new lines have no impact on comparing build result"() {
        def outputVerbose = '''> Task :run
            |Bonjour, World!
            |
            |BUILD SUCCESSFUL
            |6 actionable tasks: 6 executed'''.stripMargin()
        def outputRich = '''
            |> Task :run
            |Bonjour, World!
            |
            |BUILD SUCCESSFUL
            |6 actionable tasks: 6 executed
            |
            |
            |'''.stripMargin()

        expect:
        from(of(outputVerbose)) == from(of(outputRich))
        from(of(outputRich)).task(':run').output == 'Bonjour, World!\n'
    }
}
