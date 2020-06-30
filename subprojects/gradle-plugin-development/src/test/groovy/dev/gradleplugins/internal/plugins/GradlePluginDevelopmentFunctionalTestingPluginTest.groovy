package dev.gradleplugins.internal.plugins

import dev.gradleplugins.GradlePluginDevelopmentTestSuite
import groovy.json.JsonSlurper
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractGradlePluginDevelopmentFunctionalTestingPluginTest extends Specification {
    def project = ProjectBuilder.builder().build()

    def "creates functionalTest component"() {
        when:
        project.apply plugin: pluginIdUnderTest
        project.apply plugin: 'dev.gradleplugins.gradle-plugin-functional-test'

        then:
        project.components.findByName('functionalTest') != null
        project.components.functionalTest instanceof GradlePluginDevelopmentTestSuite
    }

    def "creates a single test task when no testing strategy"() {
        given:
        project.apply plugin: pluginIdUnderTest
        project.apply plugin: 'dev.gradleplugins.gradle-plugin-functional-test'

        when:
        project.evaluate()

        then:
        project.tasks.findByName('functionalTest') != null
        !project.tasks.functionalTest.systemProperties.containsKey('dev.gradleplugins.defaultGradleVersion')
    }

    def "creates a single test task when only one testing strategy"() {
        given:
        project.apply plugin: pluginIdUnderTest
        project.apply plugin: 'dev.gradleplugins.gradle-plugin-functional-test'
        project.gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'
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
        project.apply plugin: 'dev.gradleplugins.gradle-plugin-functional-test'
        project.gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'
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
        project.apply plugin: 'dev.gradleplugins.gradle-plugin-functional-test'
        project.gradlePlugin.compatibility.minimumGradleVersion = '6.2.1'
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

    def "can configure the test tasks before the project is evaluated"() {
        given:
        project.apply plugin: pluginIdUnderTest
        project.apply plugin: 'dev.gradleplugins.gradle-plugin-functional-test'

        when:
        project.components.functionalTest {
            testTasks.configureEach {
                systemProperty('dev.gradleplugins.samples', 'test-value')
            }
        }
        then:
        noExceptionThrown()

        when:
        project.evaluate()
        then:
        project.tasks.functionalTest.systemProperties['dev.gradleplugins.samples'] == 'test-value'
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