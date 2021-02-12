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

    def "can extract task from mixed dry-ran/verbose console output"() {
        def output = ''':compileJava SKIPPED
            |:processResources SKIPPED
            |:classes SKIPPED
            |:jar SKIPPED
            |:compileMacosCpp SKIPPED
            |:linkMacos SKIPPED
            |:jarMacos SKIPPED
            |:assembleMacos SKIPPED
            |> Task :library:compileCpp
            |> Task :library:link
            |
            |BUILD SUCCESSFUL in 788ms'''.stripMargin()

        expect:
        from(output).executedTaskPaths == [':compileJava', ':processResources', ':classes', ':jar', ':compileMacosCpp', ':linkMacos', ':jarMacos', ':assembleMacos', ':library:compileCpp', ':library:link']
        from(output).skippedTaskPaths == [':compileJava', ':processResources', ':classes', ':jar', ':compileMacosCpp', ':linkMacos', ':jarMacos', ':assembleMacos']
    }
}
