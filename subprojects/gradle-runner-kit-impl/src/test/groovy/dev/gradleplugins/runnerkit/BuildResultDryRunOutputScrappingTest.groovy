package dev.gradleplugins.runnerkit

import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.BuildResult.from

@Subject(BuildResultImpl)
class BuildResultDryRunOutputScrappingTest extends Specification {
    def "can extract task"() {
        def output = ''':foo SKIPPED
            |:bar SKIPPED
            |
            |BUILD SUCCESSFUL in 605ms'''.stripMargin()

        expect:
        from(output).executedTaskPaths == [':foo', ':bar']
        from(output).skippedTaskPaths == [':foo', ':bar']
    }

    def "can compare task"() {
        def output = ''':foo SKIPPED
            |:bar SKIPPED
            |
            |BUILD SUCCESSFUL in 605ms'''.stripMargin()

        expect:
        from(output).executedTaskPaths == [':foo', ':bar']
        from(output).skippedTaskPaths == [':foo', ':bar']
        from(output).tasks(TaskOutcome.SKIPPED)*.path == [':foo', ':bar']
    }

    def "can compare verbose and dry-run of entirely skipped tasks"() {
        def outputVerbose = '''> Task :foo SKIPPED
            |> Task :bar SKIPPED
            |
            |BUILD SUCCESSFUL in 474ms'''.stripMargin()
        def outputDryRun = ''':foo SKIPPED
            |:bar SKIPPED
            |
            |BUILD SUCCESSFUL in 605ms'''.stripMargin()

        expect:
        from(outputVerbose) == from(outputDryRun)
    }
}
