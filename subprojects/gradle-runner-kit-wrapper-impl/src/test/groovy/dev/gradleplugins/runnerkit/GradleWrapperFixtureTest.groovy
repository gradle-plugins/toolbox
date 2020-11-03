package dev.gradleplugins.runnerkit

import dev.gradleplugins.fixtures.file.FileSystemFixture
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GradleWrapperFixtureTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "can write wrapper structure on disk"() {
        when:
        GradleWrapperFixture.writeGradleWrapperTo(testDirectory)

        then:
        file('gradlew').exists()
        file('gradlew.bat').exists()
        file('gradle/wrapper/gradle-wrapper.properties').exists()
        file('gradle/wrapper/gradle-wrapper.jar').exists()
    }

    def "gradle wrapper shell script is executable"() {
        when:
        GradleWrapperFixture.writeGradleWrapperTo(testDirectory)

        then:
        file('gradlew').canExecute()
    }
}
