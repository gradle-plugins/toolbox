package dev.gradleplugins

import dev.gradleplugins.runnerkit.BuildResult

import static org.hamcrest.Matchers.*
import static spock.util.matcher.HamcrestSupport.that

abstract class AbstractGradlePluginDevelopmentTestingOrderFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec {
    def setup() {
        makeSingleProject()
    }

    def "runs unit test before functional test on check"() {
        when:
        BuildResult result = succeeds('check').delegate

        then:
        that result.executedTaskPaths, containsInRelativeOrder(':test', ':functionalTest')
    }

    def "runs unit test before any functional tests"() {
        given:
        buildFile << '''
            functionalTest {
                testingStrategies = [strategies.coverageForGradleVersion('6.9'), strategies.coverageForGradleVersion('7.0')]
            }
        '''

        when:
        BuildResult result = succeeds(':test', ':functionalTest6.9').delegate
        then:
        that result.executedTaskPaths, containsInRelativeOrder(':test', ':functionalTest6.9')

        when:
        result = succeeds(':functionalTest7.0', ':test').delegate
        then:
        that result.executedTaskPaths, containsInRelativeOrder(':test', ':functionalTest7.0')
    }

    def "runs all unit test before functional test on check"() {
        given:
        buildFile << '''
            test {
                testingStrategies = [strategies.coverageForGradleVersion('6.9'), strategies.coverageForGradleVersion('7.0')]
            }
        '''

        when:
        BuildResult result = succeeds('check').delegate

        then:
        that result.executedTaskPaths, containsInRelativeOrder(':test6.9', ':functionalTest')
        that result.executedTaskPaths, containsInRelativeOrder(':test7.0', ':functionalTest')
    }

    def "runs any unit test before functional test"() {
        given:
        buildFile << '''
            test {
                testingStrategies = [strategies.coverageForGradleVersion('6.9'), strategies.coverageForGradleVersion('7.0')]
            }
        '''

        when:
        BuildResult result = succeeds(':test6.9', ':functionalTest').delegate
        then:
        that result.executedTaskPaths, containsInRelativeOrder(':test6.9', ':functionalTest')

        when:
        result = succeeds(':functionalTest', ':test7.0').delegate
        then:
        that result.executedTaskPaths, containsInRelativeOrder(':test7.0', ':functionalTest')
    }

    def "does not runs any unit test when functional testing only"() {
        given:
        buildFile << '''
            test {
                testingStrategies = [strategies.coverageForGradleVersion('6.9'), strategies.coverageForGradleVersion('7.0')]
            }
        '''

        when:
        BuildResult result = succeeds(':functionalTest').delegate
        then:
        that result.executedTaskPaths, not(hasItem(':test'))
        that result.executedTaskPaths, not(hasItem(':test6.9'))
        that result.executedTaskPaths, not(hasItem(':test7.0'))
    }

    protected abstract String getPluginIdUnderTest()

    protected void makeSingleProject() {
        settingsFile << 'rootProject.name = "gradle-plugin"'
        buildFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-functional-test'
                id 'dev.gradleplugins.gradle-plugin-unit-test'
                id '${pluginIdUnderTest}'
            }
        """
    }
}

class GroovyGradlePluginDevelopmentTestingOrderFunctionalTest extends AbstractGradlePluginDevelopmentTestingOrderFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
}

class JavaGradlePluginDevelopmentTestingOrderFunctionalTest extends AbstractGradlePluginDevelopmentTestingOrderFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
}
