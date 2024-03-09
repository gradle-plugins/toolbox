package com.example

import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class VersionAwareFunctionalTest extends Specification {
    private static final String DEFAULT_GRADLE_VERSION_SYSPROP_NAME = 'dev.gradleplugins.defaultGradleVersion'
    @TempDir Path testProjectDir
    File settingsFile
    File buildFile

    def setup() {
        settingsFile = testProjectDir.resolve('settings.gradle').toFile()
        buildFile = testProjectDir.resolve('build.gradle').toFile()

        if (System.properties.containsKey(DEFAULT_GRADLE_VERSION_SYSPROP_NAME)) {
            println "Default Gradle version: ${gradleDistributionUnderTest}"
        } else {
            println "No default Gradle version"
        }
    }

    private static String getGradleDistributionUnderTest() {
        String defaultGradleVersionUnderTest = System.getProperty(DEFAULT_GRADLE_VERSION_SYSPROP_NAME, null)
        if (defaultGradleVersionUnderTest == null) {
            return GradleVersion.current().version;
        }
        return defaultGradleVersionUnderTest
    }

    def "print gradle version from within the executor"() {
        given:
        settingsFile << "rootProject.name = 'hello-world'"
        buildFile << '''
            plugins {
                id('com.example.hello')
            }

            println "Using Gradle version: ${project.gradle.gradleVersion}"
        '''

        when:
        def result = GradleRunner.create()
                .withPluginClasspath()
                .forwardOutput()
                .withGradleVersion(gradleDistributionUnderTest)
                .withProjectDir(testProjectDir.toFile())
                .withArguments('help')
                .build()

        then:
        result.output.contains('Hello')
    }
}