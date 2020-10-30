package dev.gradleplugins.fixtures.gradle.runner

import dev.nokee.core.exec.CommandLineToolLogContent
import spock.lang.Specification

import static dev.gradleplugins.fixtures.gradle.runner.GradleBuildResultImpl.from
import static dev.nokee.core.exec.CommandLineToolLogContent.of

class GradleBuildResultDryRunOutputScrappingTest extends Specification {
    def "can extract task"() {
        def output = ''':foo SKIPPED
            |:bar SKIPPED
            |
            |BUILD SUCCESSFUL in 605ms'''.stripMargin()

        expect:
        from(of(output)).executedTaskPaths == [':foo', ':bar']
        from(of(output)).skippedTaskPaths == [':foo', ':bar']
    }

    def "can compare task"() {
        def output = ''':foo SKIPPED
            |:bar SKIPPED
            |
            |BUILD SUCCESSFUL in 605ms'''.stripMargin()

        expect:
        from(of(output)).executedTaskPaths == [':foo', ':bar']
        from(of(output)).skippedTaskPaths == [':foo', ':bar']
        from(of(output)).tasks(GradleTaskOutcome.SKIPPED)*.path == [':foo', ':bar']
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
        from(of(outputVerbose)) == from(of(outputDryRun))
    }
}
