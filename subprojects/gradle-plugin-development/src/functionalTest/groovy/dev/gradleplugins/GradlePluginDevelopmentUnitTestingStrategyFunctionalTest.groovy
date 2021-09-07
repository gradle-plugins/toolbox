package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.*
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import groovy.json.JsonSlurper
import org.gradle.util.GradleVersion
import org.hamcrest.Matchers
import spock.lang.Unroll

abstract class AbstractGradlePluginDevelopmentUnitTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec {
    @Unroll
    def "can use testing strategy to change test runtime dependencies"(coverage, expectedVersion) {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                compatibility {
                    minimumGradleVersion = '6.2'
                }
            }

            test {
                testingStrategies = [strategies.${coverage}]
                testTasks.configureEach {
                    classpath = files(testingStrategy.map {
                        project.getConfigurations().detachedConfiguration(dependencies.gradleApi(it.version))
                    }.orElse([])) + classpath
                }
            }
        """

        when:
        succeeds('check')

        then:
        result.assertTaskNotSkipped(':test')

        and:
        testResult.assertTestClassesExecuted('com.example.VersionAwareTest')
        testResult.testClass('com.example.VersionAwareTest').assertTestPassed('printGradleVersionFromTest')
        testResult.testClass('com.example.VersionAwareTest').assertStdout(Matchers.containsString("Default Gradle version: ${expectedVersion}"))
        testResult.testClass('com.example.VersionAwareTest').assertStdout(Matchers.containsString("Using Gradle version: ${expectedVersion}"))
        testResult.testClass('com.example.VersionAwareTest').assertTestCount(1, 0, 0)

        where:
        coverage                                    | expectedVersion
        'coverageForMinimumVersion'                 | '6.2'
        // Note: nightly Gradle API are not published, so there is that.
        'coverageForLatestGlobalAvailableVersion'   | latestGlobalAvailableVersion
        'coverageForGradleVersion("6.5")'           | '6.5'
    }

    private String getLatestGlobalAvailableVersion() {
        return new JsonSlurper().parse(new URL('https://services.gradle.org/versions/current')).version
    }

    protected DefaultTestExecutionResult getTestResult() {
        new DefaultTestExecutionResult(testDirectory, 'build', '', '', 'test')
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract GradlePluginElement getComponentUnderTest()

    protected void makeSingleProject() {
        settingsFile << "rootProject.name = 'gradle-plugin'"
        buildFile << """
            plugins {
                id '${pluginIdUnderTest}'
                id 'dev.gradleplugins.gradle-plugin-unit-test'
            }

            import ${GradleVersion.canonicalName}

            gradlePlugin {
                plugins {
                    hello {
                        id = '${componentUnderTest.pluginId}'
                        implementationClass = 'com.example.BasicPlugin'
                    }
                }
            }

            repositories {
                mavenCentral()
            }

            test {
                dependencies {
                    implementation(platform('org.junit:junit-bom:5.7.2'))
                    implementation('org.junit.jupiter:junit-jupiter')
                }

                // Because of JUnit 5
                testTasks.configureEach { useJUnitPlatform() }
            }
        """
    }
}

class GroovyGradlePluginDevelopmentUnitTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentUnitTestingStrategyFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new TestableGradlePluginElement(new GroovyBasicGradlePlugin(), new GradleVersionAwareProjectBuilderTest())
    }
}

class JavaGradlePluginDevelopmentUnitTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentUnitTestingStrategyFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new TestableGradlePluginElement(new JavaBasicGradlePlugin(), new GradleVersionAwareProjectBuilderTest())
    }
}
