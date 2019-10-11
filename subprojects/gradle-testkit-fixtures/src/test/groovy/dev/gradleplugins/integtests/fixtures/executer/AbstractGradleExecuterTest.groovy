package dev.gradleplugins.integtests.fixtures.executer

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.file.TestNameTestDirectoryProvider
import org.junit.Rule
import spock.lang.Specification

abstract class AbstractGradleExecuterTest extends Specification {
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

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
        String settingsFileContent = '''println("The executer is using '${settingsDir}' as its working directory")'''
        file('settings.gradle') << settingsFileContent
        def workingDirectory = file('other-directory')
        workingDirectory.file('settings.gradle') << settingsFileContent

        when:
        def result = executerUnderTest.inDirectory(workingDirectory).run()

        then:
        result.output.contains("The executer is using '${workingDirectory}' as its working directory")
    }

    // TODO: Can change project directory while keeping working directory
    // TODO: Can relocate build.gradle
    // TODO: Can have before execute action
    // TODO: Can have after execute action
    // TODO: Can assert run successfully (no throw and throw)
    // TODO: Can assert run with failure (no throw and throw)
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
