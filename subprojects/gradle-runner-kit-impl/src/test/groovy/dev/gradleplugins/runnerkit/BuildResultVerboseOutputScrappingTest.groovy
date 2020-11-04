package dev.gradleplugins.runnerkit

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.BuildResult.from
import static dev.gradleplugins.runnerkit.TaskOutcome.SUCCESS
import static dev.gradleplugins.runnerkit.TaskOutcome.values
import static dev.gradleplugins.runnerkit.TaskOutcomeUtils.toString

@Subject(BuildResultImpl)
class BuildResultVerboseOutputScrappingTest extends Specification {

    def "can normalize successful build result"() {
        expect:
        def result = from('''> Task :foo
            |
            |BUILD SUCCESSFUL in 490ms
            |1 actionable task: 1 executed'''.stripMargin())
        result.output == '''> Task :foo
            |
            |BUILD SUCCESSFUL
            |1 actionable task: 1 executed'''.stripMargin()
    }

    def "can normalize failed build result"() {
        expect:
        def result = from('''> Task :foo FAILED
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
            |1 actionable task: 1 executed'''.stripMargin())
        result.output == '''> Task :foo FAILED
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
            |BUILD FAILED
            |1 actionable task: 1 executed'''.stripMargin()
    }

    def "last task before failure doesn't include failure in task output"() {
        given:
        def output = '''> Task :foo FAILED
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

        expect:
        from(output).task(':foo').output.empty
    }

    def "two build result of exactly same log are equals"() {
        given:
        def output = '''> Task :foo
            |
            |BUILD SUCCESSFUL in 490ms
            |1 actionable task: 1 executed'''.stripMargin()
        expect:
        from(output) == from(output)
    }

    def "two build result of different log are not equals"() {
        given:
        def output1 = '''> Task :foo
            |
            |BUILD SUCCESSFUL in 490ms
            |1 actionable task: 1 executed'''.stripMargin()
        def output2 = '''> Task :foo
            |> Task :bar
            |
            |BUILD SUCCESSFUL in 443ms
            |2 actionable tasks: 2 executed'''.stripMargin()
        expect:
        from(output1) != from(output2)
    }

    def "two build result with different timing but same tasks execution/outcome are equals"() {
        given:
        def output1 = '''> Task :foo
            |
            |BUILD SUCCESSFUL in 490ms
            |1 actionable task: 1 executed'''.stripMargin()
        def output2 = '''> Task :foo
            |
            |BUILD SUCCESSFUL in 443ms
            |1 actionable task: 1 executed'''.stripMargin()
        expect:
        from(output1) == from(output2)
    }

    def "two build result with different build result but same tasks execution/outcome are not equals"() {
        given:
        def outputSuccessful = '''> Task :foo
            |
            |BUILD SUCCESSFUL in 490ms
            |1 actionable task: 1 executed'''.stripMargin()
        def outputFailed = '''> Task :foo FAILED
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
        expect:
        from(outputSuccessful) != from(outputFailed)
    }

    @Unroll
    def "two build result with same task but different outcome are not equals"(outcome) {
        given:
        def output1 = '''> Task :foo
            |> Task :bar
            |
            |BUILD SUCCESSFUL in 488ms
            |2 actionable tasks: 2 executed'''.stripMargin()
        def output2 = """> Task :foo ${outcome}
            |> Task :bar ${outcome}
            |
            |BUILD SUCCESSFUL in 488ms""".stripMargin()

        expect:
        from(output1) != from(output2)

        where:
        outcome << [values().findAll { it != SUCCESS }.collect { toString(it) }]
    }

    def "can capture executed tasks"() {
        given:
        def output = '''> Task :foo
            |> Task :bar
            |
            |BUILD SUCCESSFUL in 488ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(output).executedTaskPaths == [':foo', ':bar']
        from(output).skippedTaskPaths == []
    }

    def "last task doesn't contain build result and post build output"() {
        given:
        def output = '''> Task :foo
            |> Task :bar
            |
            |BUILD SUCCESSFUL in 488ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(output).task(':bar').output == ''
    }

    def "can capture task output with newline"() {
        given:
        def output = '''
            |> Task :foo
            |Hello, world!
            |
            |> Task :bar
            |Goodbye, world!
            |
            |BUILD SUCCESSFUL in 506ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(output).task(':foo').output == 'Hello, world!\n'
        from(output).task(':bar').output == 'Goodbye, world!\n'
    }

    def "can capture task output without newline"() {
        given:
        def output = '''
            |> Task :foo
            |Hello, world!
            |> Task :bar
            |Goodbye, world!
            |BUILD SUCCESSFUL in 506ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(output).task(':foo').output == 'Hello, world!'
        from(output).task(':bar').output == 'Goodbye, world!'
    }

    def "can capture intertwine task output"() {
        given:
        def output = '''
            |> Task :foo
            |Hello,
            |> Task :bar
            |Goodbye, world!
            |
            |> Task :foo
            | world!
            |
            |BUILD SUCCESSFUL in 506ms
            |2 actionable tasks: 2 executed'''.stripMargin()

        expect:
        from(output).task(':foo').output == 'Hello, world!\n'
        from(output).task(':bar').output == 'Goodbye, world!\n'
    }

    @Ignore("Warnings for the test task should probably be ignored so we can compare the results")
    def "ignores reflection warnings"() {
        def output = '''> Task :test
            |WARNING: An illegal reflective access operation has occurred
            |WARNING: Illegal reflective access by org.codehaus.groovy.reflection.CachedClass (file:/Users/daniel/.gradle/wrapper/dists/gradle-6.5-bin/6nifqtx7604sqp1q6g8wikw7p/gradle-6.5/lib/groovy-all-1.3-2.5.11.jar) to method java.lang.Object.finalize()
            |WARNING: Please consider reporting this to the maintainers of org.codehaus.groovy.reflection.CachedClass
            |WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
            |WARNING: All illegal access operations will be denied in a future release
            |BUILD SUCCESSFUL in 3s
            |1 actionable task: 1 executed'''.stripMargin()

        expect:
        from(output).task(':test').output == ''
    }

    def "can create result from output"() {
        when:
        def result = from('''Starting a Gradle Daemon (subsequent builds will be faster)

> Configure project :coreExec
The Gradle Plugin Development team recommends using 'dev.gradleplugins.java-gradle-plugin' instead of 'java-gradle-plugin' in project ':coreExec'.

> Task :languageBase:jar
:languageBase:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :runtimeBase:compileJava
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.

> Task :runtimeBase:jar
:runtimeBase:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :testingBase:jar
:testingBase:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :runtimeNative:compileJava
/Users/daniel/gradle/wt-refactoring/subprojects/runtime-native/src/main/java/dev/nokee/runtime/nativebase/internal/DefaultLibraryElements.java:6: warning: Generating equals/hashCode implementation but without a call to superclass, even though this class does not extend java.lang.Object. If this is intentional, add '@EqualsAndHashCode(callSuper=false)' to your type.
@Value
^
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
1 warning

> Task :languageSwift:jar
:languageSwift:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :languageNative:jar
:languageNative:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :runtimeDarwin:jar
:runtimeDarwin:jar: No valid plugin descriptors were found in META-INF/gradle-plugins

> Task :platformNative:compileJava
Note: /Users/daniel/gradle/wt-refactoring/subprojects/platform-native/src/main/java/dev/nokee/platform/nativebase/NativeLibraryComponentDependencies.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.

BUILD SUCCESSFUL in 24s
71 actionable tasks: 29 executed, 42 up-to-date
''')

        then:
        result.executedTaskPaths == [':languageBase:jar', ':runtimeBase:compileJava', ':runtimeBase:jar', ':testingBase:jar', ':runtimeNative:compileJava', ':languageSwift:jar', ':languageNative:jar', ':runtimeDarwin:jar', ':platformNative:compileJava']
    }
}


/*
Starting Gradle Daemon...
Gradle Daemon started in 879 ms
> Task :buildSrc:compileJava NO-SOURCE
> Task :buildSrc:compileGroovy UP-TO-DATE
> Task :buildSrc:pluginDescriptors UP-TO-DATE
> Task :buildSrc:processResources UP-TO-DATE
> Task :buildSrc:classes UP-TO-DATE
> Task :buildSrc:jar UP-TO-DATE
> Task :buildSrc:assemble UP-TO-DATE
> Task :buildSrc:pluginUnderTestMetadata UP-TO-DATE
> Task :buildSrc:compileTestJava NO-SOURCE
> Task :buildSrc:compileTestGroovy NO-SOURCE
> Task :buildSrc:processTestResources NO-SOURCE
> Task :buildSrc:testClasses UP-TO-DATE
> Task :buildSrc:test NO-SOURCE
> Task :buildSrc:validatePlugins UP-TO-DATE
> Task :buildSrc:check UP-TO-DATE
> Task :buildSrc:build UP-TO-DATE
> Task :gradle-fixtures:processResources NO-SOURCE
> Task :gradle-fixtures-runner-base:processResources NO-SOURCE
> Task :gradle-fixtures-runner-base:createWrapper UP-TO-DATE
> Task :gradle-fixtures-runner-base:processTestFixturesResources UP-TO-DATE
> Task :gradle-fixtures-runner-base:processTestResources NO-SOURCE
> Task :gradle-fixtures-file-system:compileJava UP-TO-DATE
> Task :gradle-fixtures-file-system:compileGroovy NO-SOURCE
> Task :gradle-fixtures-file-system:processResources NO-SOURCE
> Task :gradle-fixtures-file-system:classes UP-TO-DATE
> Task :gradle-fixtures-file-system:jar UP-TO-DATE
> Task :gradle-fixtures-file-system:compileTestFixturesJava UP-TO-DATE
> Task :gradle-fixtures-file-system:compileTestFixturesGroovy NO-SOURCE
> Task :gradle-fixtures-file-system:processTestFixturesResources NO-SOURCE
> Task :gradle-fixtures-file-system:testFixturesClasses UP-TO-DATE
> Task :gradle-fixtures-file-system:testFixturesJar UP-TO-DATE
> Task :gradle-fixtures:compileJava UP-TO-DATE
> Task :gradle-fixtures:compileGroovy NO-SOURCE
> Task :gradle-fixtures:classes UP-TO-DATE
> Task :gradle-fixtures:jar UP-TO-DATE
> Task :gradle-fixtures-runner-base:compileJava
/Users/daniel/gradle/toolbox/subprojects/gradle-fixtures-runner-base/src/main/java/dev/gradleplugins/fixtures/gradle/runner/logging/GradleLogContent.java:36: error: missing return statement
    }
    ^
Note: /Users/daniel/gradle/toolbox/subprojects/gradle-fixtures-runner-base/src/main/java/dev/gradleplugins/fixtures/gradle/runner/parameters/GradleExecutionParameter.java uses unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
1 error
> Task :gradle-fixtures-runner-base:compileJava FAILED
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':gradle-fixtures-runner-base:compileJava'.
> Compilation failed; see the compiler error output for details.
* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
* Get more help at https://help.gradle.org
BUILD FAILED in 9s
9 actionable tasks: 1 executed, 8 up-to-date
 */