package dev.gradleplugins.fixtures.gradle.runner

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.gradle.GradleScriptFixture
import dev.gradleplugins.test.fixtures.gradle.executer.internal.OutputScrapingExecutionResult
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractGradleRunnerIntegrationTest extends Specification implements FileSystemFixture, GradleScriptFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    protected abstract GradleRunner runner(String... arguments)

    protected static String helloWorldTask() {
        """
        task helloWorld {
            doLast {
                println 'Hello world!'
            }
        }
        """
    }

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    //region Arguments
    def "can execute build without specifying any arguments"() {
        given:
        buildFile << """
            help {
                doLast {
                    file('out.txt').text = "help"
                }
            }
        """

        when:
        runner().build()

        then:
        file("out.txt").text == "help"
    }

    def "can execute build with multiple tasks"() {
        given:
        buildFile <<  """
            task t1 {
                doLast {
                    file("out.txt").text = "t1"
                }
            }
            task t2 {
                doLast {
                    file("out.txt") << "t2"
                }
            }
        """

        when:
        runner('t1', 't2').build()

        then:
        file("out.txt").text == "t1t2"
    }

    def "can provide non task arguments"() {
        given:
        buildFile << """
            task writeValue {
                doLast {
                    file("out.txt").text = project.value
                }
            }
        """

        when:
        runner("writeValue", "-Pvalue=foo").build()

        then:
        file("out.txt").text == "foo"
    }

//    def "can enable parallel execution via --parallel property"() {
//        given:
//        buildFile << """
//            task writeValue {
//                doLast {
//                    file("out.txt").text = gradle.startParameter.parallelProjectExecutionEnabled
//                }
//            }
//        """
//
//        when:
//        runner("writeValue", "--parallel")
//                .withGradleVersion(determineMinimumVersionThatRunsOnCurrentJavaVersion("4.1"))
//                .build()
//
//        then:
//        file("out.txt").text == "true"
//    }
//
//    @NotYetImplemented
//    @Issue("GRADLE-3563")
//    def "can enable parallel execution via system property"() {
//        given:
//        buildFile << """
//            task writeValue {
//                doLast {
//                    file("out.txt").text = gradle.startParameter.parallelProjectExecutionEnabled
//                }
//            }
//        """
//
//        when:
//        runner("writeValue", "-Dorg.gradle.parallel=true")
//                .withGradleVersion("3.1")
//                .build()
//
//        then:
//        file("out.txt").text == "true"
//    }
    //endregion

    //region Build failures
    /*
    Note: these tests are very granular to ensure coverage for versions that
          don't support querying the output or tasks.
 */

    def "does not throw exception when build fails expectantly"() {
        given:
        buildFile << """
            task helloWorld {
                doLast {
                    throw new GradleException('Expected exception')
                }
            }
        """

        when:
        runner('helloWorld').buildAndFail()

        then:
        noExceptionThrown()
    }

////    @InspectsBuildOutput
////    @InspectsExecutedTasks
//    def "exposes result when build fails expectantly"() {
//        given:
//        buildFile << """
//            task helloWorld {
//                doLast {
//                    throw new GradleException('Expected exception')
//                }
//            }
//        """
//
//        when:
//        def result = runner('helloWorld').buildAndFail()
//
//        then:
//        result.taskPaths(FAILED) == [':helloWorld']
//        result.output.contains("Expected exception")
//    }
//
//    def "throws when build is expected to fail but does not"() {
//        given:
//        buildFile << helloWorldTask()
//
//        when:
//        runner('helloWorld').buildAndFail()
//
//        then:
//        def t = thrown(UnexpectedBuildSuccess)
//        t.buildResult != null
//    }
//
////    @InspectsBuildOutput
////    @InspectsGroupedOutput
////    @InspectsExecutedTasks
//    def "exposes result when build is expected to fail but does not"() {
//        given:
//        buildFile << helloWorldTask()
//
//        when:
//        def runner = gradleVersion >= GradleVersion.version("4.5")
//                ? runner('helloWorld', '--warning-mode=none')
//                : runner('helloWorld')
//        runner.buildAndFail()
//
//        then:
//        def t = thrown(UnexpectedBuildSuccess)
//        def expectedMessage = """Unexpected build execution success in ${testDirectory.canonicalPath} with arguments ${runner.allArguments}
//
//Output:
//$t.buildResult.output"""
//
//        def buildOutput = OutputScrapingExecutionResult.from(t.buildResult.output, "")
//        buildOutput.assertTasksExecuted(":helloWorld")
//        buildOutput.groupedOutput.task(":helloWorld").output == "Hello world!"
//
//        normaliseLineSeparators(t.message).startsWith(normaliseLineSeparators(expectedMessage))
//        t.buildResult.taskPaths(SUCCESS) == [':helloWorld']
//    }
//
//    def "throws when build is expected to succeed but fails"() {
//        given:
//        buildFile << """
//            task helloWorld {
//                doLast {
//                    throw new GradleException('Unexpected exception')
//                }
//            }
//        """
//
//        when:
//        runner('helloWorld').build()
//
//        then:
//        def t = thrown UnexpectedBuildFailure
//        t.buildResult != null
//    }
//
////    @InspectsExecutedTasks
////    @InspectsBuildOutput
//    def "exposes result with build is expected to succeed but fails "() {
//        given:
//        buildFile << """
//            task helloWorld {
//                doLast {
//                    throw new GradleException('Unexpected exception')
//                }
//            }
//        """
//
//        when:
//        def runner = runner('helloWorld')
//        runner.build()
//
//        then:
//        UnexpectedBuildFailure t = thrown(UnexpectedBuildFailure)
//        String expectedMessage = """Unexpected build execution failure in ${testDirectory.canonicalPath} with arguments ${runner.allArguments}
//
//Output:
//$t.buildResult.output"""
//
//        def failure = OutputScrapingExecutionFailure.from(t.buildResult.output, "")
//        failure.assertTasksExecuted(':helloWorld')
//        failure.assertHasDescription("Execution failed for task ':helloWorld'.")
//        failure.assertHasCause('Unexpected exception')
//
//        normaliseLineSeparators(t.message).startsWith(normaliseLineSeparators(expectedMessage))
//        t.buildResult.taskPaths(FAILED) == [':helloWorld']
//    }
    //endregion


    //region Capture output
    static final String OUT = "-- out --"
    static final String ERR = "-- err --"

    @Rule
    RedirectStdOutAndErr stdStreams = new RedirectStdOutAndErr()

    def "can capture stdout and stderr"() {
        given:
        def standardOutput = new StringWriter()
        def standardError = new StringWriter()
        buildFile << helloWorldWithStandardOutputAndError()

        when:
        def result = runner('helloWorld', "-d", "-s")
                .forwardStdOutput(standardOutput)
                .forwardStdError(standardError)
                .build()

        then:
        result.output.findAll(OUT).size() == 1
        result.output.findAll(ERR).size() == 1
        standardOutput.toString().findAll(OUT).size() == 1
        standardError.toString().findAll(OUT).size() == 0
//        if (isCompatibleVersion('4.7')) {
//            // Handling of error log messages changed
//            standardOutput.toString().findAll(ERR).size() == 1
//            standardError.toString().findAll(ERR).size() == 0
//        } else {
            standardOutput.toString().findAll(ERR).size() == 0
            standardError.toString().findAll(ERR).size() == 1
//        }

        // isn't empty if version < 2.8 or potentially contains Gradle dist download progress output
//        if (isCompatibleVersion('2.8') && !crossVersion) {
            def output = OutputScrapingExecutionResult.from(stdStreams.stdOut, stdStreams.stdErr)
            output.normalizedOutput.empty
            output.error.empty
//        }
    }

    def "can forward test execution output to System.out and System.err"() {
        given:
        buildFile << helloWorldWithStandardOutputAndError()

        when:
        def result = runner('helloWorld')
                .forwardOutput()
                .build()

        then:
        noExceptionThrown()
        result.output.findAll(OUT).size() == 1
        result.output.findAll(ERR).size() == 1

        // prints out System.out twice for version < 2.3
//        if (isCompatibleVersion('2.3')) {
            assert stdStreams.stdOut.findAll(OUT).size() == 1
            assert stdStreams.stdOut.findAll(ERR).size() == 1
//        } else {
//            assert stdStreams.stdOut.findAll(OUT).size() == 2
//            assert stdStreams.stdOut.findAll(ERR).size() == 2
//        }
    }

    def "output is captured if unexpected build exception is thrown"() {
        given:
        Writer standardOutput = new StringWriter()
        Writer standardError = new StringWriter()
        buildFile << helloWorldWithStandardOutputAndError()

        when:
        runner('helloWorld')
                .forwardStdOutput(standardOutput)
                .forwardStdError(standardError)
                .buildAndFail()

        then:
        def t = thrown UnexpectedBuildSuccess
        def result = t.buildResult
        result.output.findAll(OUT).size() == 1
        result.output.findAll(ERR).size() == 1
        standardOutput.toString().findAll(OUT).size() == 1
        standardError.toString().findAll(OUT).size() == 0
//        if (isCompatibleVersion('4.7')) {
//            // Handling of error log messages changed
//            standardOutput.toString().findAll(ERR).size() == 1
//            standardError.toString().findAll(ERR).size() == 0
//        } else {
            standardOutput.toString().findAll(ERR).size() == 0
            standardError.toString().findAll(ERR).size() == 1
//        }
    }

    static String helloWorldWithStandardOutputAndError() {
        """
            task helloWorld {
                doLast {
                    println '$OUT'
                    System.err.println '$ERR'
                }
            }
        """
    }
    //endregion
}
