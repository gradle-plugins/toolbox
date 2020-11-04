package dev.gradleplugins.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.BuildResult.from

@Subject(BuildResultImpl)
class BuildResultRichOutputScrappingTest extends Specification {
    def "two rich output of different amount of task executed are not equals"() {
        def output1 = '''
            |BUILD SUCCESSFUL in 441ms
            |1 actionable task: 1 executed'''.stripMargin()
        def output2 = '''
            |BUILD SUCCESSFUL in 501ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(output1) != from(output2)
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
        from(outputVerbose).asRichOutputResult() == from(outputRich)
        from(outputVerbose).asRichOutputResult().executedTaskPaths == []
        from(outputVerbose).asRichOutputResult().skippedTaskPaths == []
        from(outputVerbose).asRichOutputResult().task(':foo') == null
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
        from(outputVerbose) == from(outputRich)
        from(outputRich).task(':run').output == 'Bonjour, World!\n'
    }
}
