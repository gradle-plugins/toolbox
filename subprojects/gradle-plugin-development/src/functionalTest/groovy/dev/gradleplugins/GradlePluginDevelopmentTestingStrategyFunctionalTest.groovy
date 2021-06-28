package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.*
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import groovy.json.JsonSlurper
import org.gradle.util.GradleVersion
import org.hamcrest.Matchers
import spock.lang.Unroll

abstract class AbstractGradlePluginDevelopmentTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec {
    def "checks executes all testing strategy tasks"() {
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

            functionalTest {
                testingStrategies = [strategies.coverageForMinimumVersion, strategies.coverageForLatestGlobalAvailableVersion]
            }
        """

        when:
        succeeds('check')

        then:
        result.assertTaskNotSkipped(':functionalTestMinimumGradle')
        result.assertTaskNotSkipped(':functionalTestLatestGlobalAvailable')
        result.assertTaskNotSkipped(':check')
    }

    def "does not execute test using default Gradle version"() {
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
        """

        when:
        succeeds('check')

        then:
        result.assertTaskNotSkipped(':functionalTest')

        and:
        testResult.assertTestClassesExecuted('com.example.VersionAwareFunctionalTest')
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertTestPassed('print gradle version from within the executor')
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertStdout(Matchers.containsString("No default Gradle version"))
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertTestCount(1, 0, 0)
    }

    @Unroll
    def "can execute test using the coverage default Gradle version"(coverage, expectedVersion) {
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

            functionalTest {
                testingStrategies = [strategies.${coverage}]
            }
        """

        when:
        succeeds('functionalTest')

        then:
        result.assertTaskNotSkipped(':functionalTest')

        and:
        testResult.assertTestClassesExecuted('com.example.VersionAwareFunctionalTest')
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertTestPassed('print gradle version from within the executor')
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertStdout(Matchers.containsString("Default Gradle version: ${expectedVersion}"))
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertStdout(Matchers.containsString("Using Gradle version: ${expectedVersion}"))
        testResult.testClass('com.example.VersionAwareFunctionalTest').assertTestCount(1, 0, 0)

        where:
        coverage                                    | expectedVersion
        'coverageForMinimumVersion'                 | '6.2'
        'coverageForLatestNightlyVersion'           | latestNightlyVersion
        'coverageForLatestGlobalAvailableVersion'   | latestGlobalAvailableVersion
    }

    private String getLatestNightlyVersion() {
        return new JsonSlurper().parse(new URL('https://services.gradle.org/versions/nightly')).version
    }

    private String getLatestGlobalAvailableVersion() {
        return new JsonSlurper().parse(new URL('https://services.gradle.org/versions/current')).version
    }

    protected DefaultTestExecutionResult getTestResult() {
        new DefaultTestExecutionResult(testDirectory, 'build', '', '', 'functionalTest')
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract GradlePluginElement getComponentUnderTest()

    protected void makeSingleProject() {
        settingsFile << "rootProject.name = 'gradle-plugin'"
        buildFile << """
            plugins {
                id '${pluginIdUnderTest}'
                id 'dev.gradleplugins.gradle-plugin-functional-test'
                id 'groovy-base' // for spock framework
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

            functionalTest {
                dependencies {
                    if (GradleVersion.current() > GradleVersion.version('6.5')) {
                        implementation platform('org.spockframework:spock-bom:2.0-groovy-3.0')
                    } else {
                        implementation platform('org.spockframework:spock-bom:2.0-groovy-2.5')
                    }
                    implementation 'org.spockframework:spock-core'
                    implementation gradleTestKit()
                }

                // Because spock framework 2.0 use JUnit 5
                testTasks.configureEach { useJUnitPlatform() }
            }
        """
    }
}

class GroovyGradlePluginDevelopmentTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentTestingStrategyFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new TestableGradlePluginElement(new GroovyBasicGradlePlugin(), new GradleVersionAwareTestKitFunctionalTest())
    }
}

class JavaGradlePluginDevelopmentTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentTestingStrategyFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new TestableGradlePluginElement(new JavaBasicGradlePlugin(), new GradleVersionAwareTestKitFunctionalTest())
    }
}
