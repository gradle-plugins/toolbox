package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.testkit.runner.UnexpectedBuildFailure
import org.gradle.testkit.runner.UnexpectedBuildSuccess
import org.junit.Rule
import spock.lang.Specification

abstract class AbstractGradleExecuterTest extends Specification {
    @Rule
    protected final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

    protected abstract GradleExecuter getExecuterUnderTest()

    protected TestFile file(String path) {
        return temporaryFolder.testDirectory.file(path)
    }

    def "can execute build without settings script"() {
        def settingsFile = temporaryFolder.testDirectory.file('settings.gradle')
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

    def "can assert successful execution"() {
        file('build.gradle') << '''
            if (System.properties.containsKey('throw')) {
                throw new GradleException("build failing")
            } else if (System.properties.containsKey('nothrow')) {
                println('build succeeding')
            } else {
                println('something is wrong')
            }
        '''

        when:
        def resultNoThrow = executerUnderTest.withArguments('-Dnothrow').run()

        then:
        noExceptionThrown()
        resultNoThrow.output.contains('build succeeding')

        when:
        executerUnderTest.withArguments('-Dthrow').run()

        then:
        def error = thrown(UnexpectedBuildFailure)
        error.message.contains('build failing')
    }

    def "can assert unsuccessful execution"() {
        file('build.gradle') << '''
            if (System.properties.containsKey('throw')) {
                throw new GradleException("build failing")
            } else if (System.properties.containsKey('nothrow')) {
                println('build succeeding')
            } else {
                println('something is wrong')
            }
        '''

        when:
        def resultThrow = executerUnderTest.withArguments('-Dthrow').runWithFailure()

        then:
        noExceptionThrown()
        resultThrow.output.contains('build failing')

        when:
        executerUnderTest.withArguments('-Dnothrow').runWithFailure()

        then:
        def error = thrown(UnexpectedBuildSuccess)
        error.message.contains('build succeeding')
    }

    def "can have before execute action"() {
        def resultFile = file('result.txt')
        def executer = executerUnderTest
        executer.beforeExecute { resultFile << 'GradleExecuter#beforeExecute\n' }
        file('build.gradle') << """
            file('${resultFile}') << 'build.gradle\\n'
        """

        when:
        executer.run()

        then:
        resultFile.text == '''GradleExecuter#beforeExecute
build.gradle
'''
    }

    def "can have after execute action"() {
        def resultFile = file('result.txt')
        def executer = executerUnderTest
        executer.afterExecute { resultFile << 'GradleExecuter#afterExecute\n' }
        file('build.gradle') << """
            file('${resultFile}') << 'build.gradle\\n'
        """

        when:
        executer.run()

        then:
        resultFile.text == '''build.gradle
GradleExecuter#afterExecute
'''
    }

    def "can have change user home directory"() {
        file('build.gradle') << '''
            println("User home directory is: '${System.properties['user.home']}'")
        '''

        expect:
        executerUnderTest.run().output.contains("User home directory is: '${System.properties['user.home']}'")
        executerUnderTest.withUserHomeDirectory(temporaryFolder.testDirectory).run().output.contains("User home directory is: '${temporaryFolder.testDirectory.absolutePath}'")
    }

    // TODO: Disallow argument flags that are handled by some modeling
    // TODO: Disallow tasks argument
    // TODO: Disallow listing tasks (aka `tasks`) in favor of listTasks()... maybe
    def "can add arguments on the executer"() {
        file('build.gradle') << """
            if (System.properties['foo'] != 'bar') {
                throw new GradleException("'foo' property wasn't passed")
            }
            if (System.properties['bar'] != 'foo') {
                throw new GradleException("'bar' property wasn't passed")
            }
        """

        when:
        executerUnderTest.withArgument('-Dfoo=bar').withArgument('-Dbar=foo').run()

        then:
        noExceptionThrown()
    }

    def "can replace all arguments on the executer"() {
        file('build.gradle') << """
            if (System.properties.containsKey('nofoo')) {
                throw new GradleException("'nofoo' property was passed but shouldn't")
            }
            if (System.properties['foo'] != 'bar') {
                throw new GradleException("'foo' property wasn't passed")
            }
            if (System.properties['bar'] != 'foo') {
                throw new GradleException("'bar' property wasn't passed")
            }
        """

        when:
        executerUnderTest.withArgument('-Dnofoo=nobar').withArguments('-Dfoo=bar', '-Dbar=foo').run()

        then:
        noExceptionThrown()
    }

    def "can append arguments on the executer"() {
        file('build.gradle') << """
            if (System.properties.containsKey('nofoo')) {
                throw new GradleException("'nofoo' property was passed but shouldn't")
            }
            if (System.properties['foo'] != 'bar') {
                throw new GradleException("'foo' property wasn't passed")
            }
            if (System.properties['bar'] != 'foo') {
                throw new GradleException("'bar' property wasn't passed")
            }
        """

        when:
        executerUnderTest.withArgument('-Dnofoo=nobar').withArguments('-Dfoo=bar').withArgument('-Dbar=foo').run()

        then:
        noExceptionThrown()
    }

    // TODO: withTasks execute tasks
    // TODO: with build cache enabled
    // TODO: with stacktrace disabled

    // ExecutionResult / ExecutionFailure
    // TODO: Can assert task executed and not skipped (success and not all task asserted)
    // TODO: Can assert task skipped (success and not all task asserted)
}
