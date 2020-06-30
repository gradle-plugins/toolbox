package dev.gradleplugins.internal.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import static org.gradle.api.JavaVersion.*

abstract class AbstractGradlePluginDevelopmentPluginTest extends Specification {
    def project = ProjectBuilder.builder().build()

    @Unroll
    def "configures minimum JVM compatibility based on minimum Gradle version"(minimumGradleVersion, expectedSourceCompatibility, expectedTargetCompatibility) {
        given:
        project.apply plugin: pluginIdUnderTest

        when:
        project.gradlePlugin.compatibility.minimumGradleVersion = minimumGradleVersion
        project.evaluate()

        then:
        project.java.sourceCompatibility == expectedSourceCompatibility
        project.java.targetCompatibility == expectedTargetCompatibility

        where:
        minimumGradleVersion    || expectedSourceCompatibility  | expectedTargetCompatibility
        '6.0'                   || VERSION_1_8                  | VERSION_1_8
        '2.14'                  || VERSION_1_6                  | VERSION_1_6
    }

    @Unroll
    def "can overwrite JVM compatibility"(minimumGradleVersion) {
        given:
        project.apply plugin: pluginIdUnderTest

        when:
        project.gradlePlugin.compatibility.minimumGradleVersion = minimumGradleVersion
        project.java.sourceCompatibility = VERSION_11
        project.java.targetCompatibility = VERSION_11
        project.evaluate()

        then:
        project.java.sourceCompatibility == VERSION_11
        project.java.targetCompatibility == VERSION_11

        where:
        minimumGradleVersion << ['6.0', '2.14']
    }

    protected abstract String getPluginIdUnderTest()
}

class JavaGradlePluginDevelopmentPluginTest extends AbstractGradlePluginDevelopmentPluginTest {
    final String pluginIdUnderTest = 'dev.gradleplugins.java-gradle-plugin'
}

class GroovyGradlePluginDevelopmentPluginTest extends AbstractGradlePluginDevelopmentPluginTest {
    final String pluginIdUnderTest = 'dev.gradleplugins.groovy-gradle-plugin'
}
