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
        file('settings.gradle') << 'println("Goodbye, world!")'
        def settingsFile = file('foo-settings.gradle')
        settingsFile << """
            println('Hello, world!')
        """

        when:
        def result = executerUnderTest.usingSettingsFile(settingsFile).run()

        then:
        result.output.contains('Hello, world!')
        !result.output.contains('Goodbye, world!')
    }

    def "can change working directory"() {
        file('settings.gradle') << 'println("Goodbye, world!")'
        def workingDirectory = file('other-directory')
        workingDirectory.file('settings.gradle') << 'println("Hello, world!")'

        when:
        def result = executerUnderTest.inDirectory(workingDirectory).run()

        then:
        result.output.contains('Hello, world!')
        !result.output.contains('Goodbye, world!')
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
