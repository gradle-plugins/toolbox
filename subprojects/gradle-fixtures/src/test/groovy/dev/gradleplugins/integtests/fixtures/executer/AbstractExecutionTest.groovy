//package dev.gradleplugins.integtests.fixtures.executer
//
//import dev.gradleplugins.test.fixtures.file.TestFile
//import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult
//import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
//import org.junit.Rule
//import org.junit.rules.TemporaryFolder
//import spock.lang.Specification
//
//abstract class AbstractExecutionTest extends Specification {
//    @Rule protected final TemporaryFolder temporaryFolder = new TemporaryFolder(getClass())
//
//    protected abstract GradleExecuter getExecuterUnderTest()
//
//    protected abstract ExecutionResult run(GradleExecuter executer)
//
//    protected TestFile getTestDirectory() {
//        return TestFile.of(temporaryFolder.root)
//    }
//
//    protected TestFile getBuildFile() {
//        return testDirectory.createFile("build.gradle")
//    }
//
//    protected String getTaskNameUnderTest() {
//        return "verify"
//    }
//
//    protected String getTaskUnderTestDsl() {
//        return "tasks.${taskNameUnderTest}"
//    }
//
//    def "can assert tasks were executed and not skipped"() {
//        given:
//        buildFile << """
//            tasks.all { doLast { println "task \${it.name}" } }
//
//            tasks.create('a')
//            tasks.create('b').dependsOn('a')
//            tasks.create('c').dependsOn('b')
//            ${taskUnderTestDsl}.dependsOn('c')
//        """
//        def result = run(executerUnderTest.withArguments('verify'))
//
//        when:
//        result.assertTasksExecutedAndNotSkipped(':a', ':b', ':c', ':verify')
//
//        then:
//        noExceptionThrown()
//    }
//
//    def "gives proper message when unable to assert tasks were executed and not skipped (missing task)"() {
//        given:
//        buildFile << """
//            tasks.all { doLast { println "task \${it.name}" } }
//
//            tasks.create('a')
//            tasks.create('b').dependsOn('a')
//            tasks.create('c').dependsOn('b')
//            ${taskUnderTestDsl}.dependsOn('c')
//        """
//        def result = run(executerUnderTest.withArguments('verify'))
//
//        when:
//        result.assertTasksExecutedAndNotSkipped(':a', ':b', ':c')
//
//        then:
//        def error = thrown(AssertionError)
//        println error.message
//    }
//
//    def "gives proper message when unable to assert tasks were executed and not skipped (task skipped)"() {
//        given:
//        buildFile << """
//            tasks.all { doLast { println "task \${it.name}" } }
//
//            tasks.create('a').enabled = false
//            tasks.create('b').dependsOn('a')
//            tasks.create('c').dependsOn('b')
//            ${taskUnderTestDsl}.dependsOn('c')
//        """
//        def result = run(executerUnderTest.withArguments('verify'))
//
//        when:
//        result.assertTasksExecutedAndNotSkipped(':a', ':b', ':c', ':verify')
//
//        then:
//        def error = thrown(AssertionError)
//        println error.message
//    }
//    // TODO: Also for task that were cached, no-source, up-to-date
//}
