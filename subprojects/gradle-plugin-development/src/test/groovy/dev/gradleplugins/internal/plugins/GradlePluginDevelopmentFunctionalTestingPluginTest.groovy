package dev.gradleplugins.internal.plugins

import dev.gradleplugins.GradlePluginSpockFrameworkTestSuite
import groovy.json.JsonSlurper
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractGradlePluginDevelopmentFunctionalTestingPluginTest extends Specification {
    def project = ProjectBuilder.builder().build()

    def "creates functionalTest component"() {
        when:
        project.apply plugin: pluginIdUnderTest

        then:
        project.components.findByName('functionalTest') != null
        project.components.functionalTest instanceof GradlePluginSpockFrameworkTestSuite
    }

    def "creates a single test task when no testing strategy"() {
        given:
        project.apply plugin: pluginIdUnderTest

        when:
        project.evaluate()

        then:
        project.tasks.findByName('functionalTest') != null
        !project.tasks.functionalTest.systemProperties.containsKey('dev.gradleplugins.defaultGradleVersion')
    }

    def "creates a single test task when only one testing strategy"() {
        given:
        project.apply plugin: pluginIdUnderTest
        project.gradlePlugin.extra.minimumGradleVersion = '6.2.1'
        project.components.functionalTest {
            testingStrategies = [strategies.coverageForMinimumVersion]
        }

        when:
        project.evaluate()

        then:
        project.tasks.findByName('functionalTest') != null
        project.tasks.findByName('functionalTestMinimumGradle') == null
    }

    def "creates multiple test task when multiple testing strategy"() {
        given:
        project.apply plugin: pluginIdUnderTest
        project.gradlePlugin.extra.minimumGradleVersion = '6.2.1'
        project.components.functionalTest {
            testingStrategies = [strategies.coverageForMinimumVersion, strategies.coverageForLatestNightlyVersion]
        }

        when:
        project.evaluate()

        then:
        project.tasks.findByName('functionalTestMinimumGradle') != null
        project.tasks.findByName('functionalTestLatestNightly') != null
    }

    @Unroll
    def "configures the default Gradle version settings according to the coverage"(coverage, expectedVersion) {
        given:
        project.apply plugin: pluginIdUnderTest
        project.gradlePlugin.extra.minimumGradleVersion = '6.2.1'
        project.components.functionalTest {
            testingStrategies = Optional.ofNullable(coverage).map { [strategies."$it"] }.orElse([])
        }

        when:
        project.evaluate()

        then:
        project.tasks.functionalTest.systemProperties['dev.gradleplugins.defaultGradleVersion'] == expectedVersion

        where:
        coverage                                    | expectedVersion
        null                                        | null
        'coverageForMinimumVersion'                 | '6.2.1'
        'coverageForLatestNightlyVersion'           | latestNightlyVersion
        'coverageForLatestGlobalAvailableVersion'   | latestGlobalAvailableVersion
    }

    private String getLatestNightlyVersion() {
        return new JsonSlurper().parse(new URL('https://services.gradle.org/versions/nightly')).version
    }

    private String getLatestGlobalAvailableVersion() {
        return new JsonSlurper().parse(new URL('https://services.gradle.org/versions/current')).version
    }

    protected abstract String getPluginIdUnderTest()
}

class JavaGradlePluginDevelopmentFunctionalTestingPluginTest extends AbstractGradlePluginDevelopmentFunctionalTestingPluginTest {
    final String pluginIdUnderTest = 'dev.gradleplugins.java-gradle-plugin'
}

class GroovyGradlePluginDevelopmentFunctionalTestingPluginTest extends AbstractGradlePluginDevelopmentFunctionalTestingPluginTest {
    final String pluginIdUnderTest = 'dev.gradleplugins.groovy-gradle-plugin'
}