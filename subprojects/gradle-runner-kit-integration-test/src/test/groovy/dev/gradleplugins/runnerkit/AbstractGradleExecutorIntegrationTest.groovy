package dev.gradleplugins.runnerkit

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.runnerkit.GradleRunnerFixture
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractGradleExecutorIntegrationTest extends Specification implements GradleRunnerFixture, FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "creates settings file when missing"() {
        when:
        newRunner().inDirectory(testDirectory).build()

        then:
        file('settings.gradle').exists()
    }

    def "does not overwrite settings file when present"() {
        when:
        file('settings.gradle') << 'rootProject.name = "foo"'
        newRunner().inDirectory(testDirectory).build()

        then:
        file('settings.gradle').text == 'rootProject.name = "foo"'
    }

    def "does not create settings file when ignoring missing"() {
        when:
        newRunner().inDirectory(testDirectory).ignoresMissingSettingsFile().buildAndFail()

        then:
        !file('settings.gradle').exists()
    }
}
