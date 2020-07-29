package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Specification

abstract class AbstractGradleExecuterTest extends Specification {
    @Rule
    protected final TemporaryFolder temporaryFolder = new TemporaryFolder()

    protected abstract GradleExecuter getExecuterUnderTest()

    protected TestFile getTestDirectory() {
        return TestFile.of(temporaryFolder.root)
    }

    protected TestFile file(String path) {
        return testDirectory.file(path)
    }

    def "can execute build without settings script"() {
        def settingsFile = testDirectory.file('settings.gradle')
        settingsFile.assertDoesNotExist()

        when:
        executerUnderTest.run()

        then:
        noExceptionThrown()
        settingsFile.assertExists()
    }

    def "can relocate settings.gradle"() {
        String settingsFileContent = '''println("The executer is using '${buildscript.sourceFile}' as its settings file")'''
        file('settings.gradle') << settingsFileContent
        def settingsFile = file('foo-settings.gradle')
        settingsFile << settingsFileContent

        when:
        def result = executerUnderTest.usingSettingsFile(settingsFile).run()

        then:
        result.output.contains("The executer is using '${settingsFile}' as its settings file")
    }

    def "can change working directory"() {
        // TODO: We should probably use System.getProperty("user.dir") but TestKit seems to be embedded
        String settingsFileContent = '''println("The executer is using '${settingsDir}' as its working directory")'''
        file('settings.gradle') << settingsFileContent
        def workingDirectory = file('other-directory')
        workingDirectory.file('settings.gradle') << settingsFileContent

        when:
        def result = executerUnderTest.inDirectory(workingDirectory).run()

        then:
        result.output.contains("The executer is using '${workingDirectory}' as its working directory")
    }

    @Ignore
    def "can change project directory while keeping working directory"() {
        // TODO: We should probably also check System.getProperty("user.dir") but TestKit seems to be embedded
        String settingsFileContent = '''println("The executer is using '${settingsDir}' as its project directory")'''
        file('settings.gradle') << settingsFileContent
        def projectDirectory = file('other-directory')
        projectDirectory.file('settings.gradle') << settingsFileContent

        when:
        def result = executerUnderTest.usingProjectDirectory(projectDirectory).run()

        then:
        result.output.contains("The executer is using '${projectDirectory}' as its project directory")
    }

    def "can relocate build.gradle"() {
        String buildFileContent = '''println("The executer is using '${buildscript.sourceFile}' as its build file")'''
        file('build.gradle') << buildFileContent
        def buildFile = file('foo-build.gradle')
        buildFile << buildFileContent

        when:
        def result = executerUnderTest.usingSettingsFile(buildFile).run()

        then:
        result.output.contains("The executer is using '${buildFile}' as its build file")
    }

//    def "can assert successful execution"() {
//        file('build.gradle') << '''
//            if (System.properties.containsKey('throw')) {
//                throw new GradleException("build failing")
//            } else if (System.properties.containsKey('nothrow')) {
//                println('build succeeding')
//            } else {
//                println('something is wrong')
//            }
//        '''
//
//        when:
//        def resultNoThrow = executerUnderTest.withArguments('-Dnothrow').run()
//
//        then:
//        noExceptionThrown()
//        resultNoThrow.output.contains('build succeeding')
//
//        when:
//        executerUnderTest.withArguments('-Dthrow').run()
//
//        then:
//        def error = thrown(UnexpectedBuildFailure)
//        error.message.contains('build failing')
//    }

//    def "can assert unsuccessful execution"() {
//        file('build.gradle') << '''
//            if (System.properties.containsKey('throw')) {
//                throw new GradleException("build failing")
//            } else if (System.properties.containsKey('nothrow')) {
//                println('build succeeding')
//            } else {
//                println('something is wrong')
//            }
//        '''
//
//        when:
//        def resultThrow = executerUnderTest.withArguments('-Dthrow').runWithFailure()
//
//        then:
//        noExceptionThrown()
//        resultThrow.output.contains('build failing')
//
//        when:
//        executerUnderTest.withArguments('-Dnothrow').runWithFailure()
//
//        then:
//        def error = thrown(UnexpectedBuildSuccess)
//        error.message.contains('build succeeding')
//    }
    // TODO: Can have before execute action
    // TODO: Can have after execute action
    // TODO: Can change home user directory (one run with and one run without)
    // TODO: withArguments replace all arguments
    // TODO: withArgument adds arguments
    // TODO: withTasks execute tasks
    // TODO: with build cache enabled
    // TODO: with stacktrace disabled

    // ExecutionResult / ExecutionFailure
    // TODO: Can assert task executed and not skipped (success and not all task asserted)
    // TODO: Can assert task skipped (success and not all task asserted)
}
