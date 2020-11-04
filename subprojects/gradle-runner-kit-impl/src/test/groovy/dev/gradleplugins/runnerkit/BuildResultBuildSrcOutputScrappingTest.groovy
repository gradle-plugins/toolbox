package dev.gradleplugins.runnerkit

import spock.lang.Specification

import static dev.gradleplugins.runnerkit.BuildResult.from

class BuildResultBuildSrcOutputScrappingTest extends Specification {
    def "includes buildSrc tasks"() {
        expect:
        from(output).executedTaskPaths == [':buildSrc:compileJava', ':buildSrc:compileGroovy', ':buildSrc:processResources', ':buildSrc:classes', ':buildSrc:jar', ':buildSrc:assemble', ':buildSrc:compileTestJava', ':buildSrc:compileTestGroovy', ':buildSrc:processTestResources', ':buildSrc:testClasses', ':buildSrc:test', ':buildSrc:check', ':buildSrc:build', ':foo']
        from(output).skippedTaskPaths == [':buildSrc:compileJava', ':buildSrc:compileGroovy', ':buildSrc:processResources', ':buildSrc:classes', ':buildSrc:compileTestJava', ':buildSrc:compileTestGroovy', ':buildSrc:processTestResources', ':buildSrc:testClasses', ':buildSrc:test', ':buildSrc:check']
    }

    def "can ignore buildSrc project from build result"() {
        expect:
        from(output).withoutBuildSrc().executedTaskPaths == [':foo']
        from(output).withoutBuildSrc().skippedTaskPaths == []
    }

    private static String getOutput() {
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
