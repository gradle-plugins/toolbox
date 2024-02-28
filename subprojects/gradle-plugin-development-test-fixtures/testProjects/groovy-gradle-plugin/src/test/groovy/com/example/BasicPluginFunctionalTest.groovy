package com.example

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification
import java.nio.file.Path

class BasicPluginFunctionalTest extends Specification {
    @TempDir Path testProjectDir
    File settingsFile
    File buildFile

    def setup() {
        settingsFile = testProjectDir.resolve('settings.gradle').toFile()
        buildFile = testProjectDir.resolve('build.gradle').toFile()
    }

    def "can do basic test"() {
        given:
        settingsFile << "rootProject.name = 'hello-world'"
        buildFile << '''
            plugins {
                id('com.example.hello')
            }
        '''

        when:
        def result = GradleRunner.create()
                .withPluginClasspath()
                .forwardOutput()
                .withProjectDir(testProjectDir.toFile())
                .withArguments('help')
                .withGradleVersion('7.4')
                .build()

        then:
        result.output.contains('Hello')
    }
}