package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import dev.gradleplugins.test.fixtures.scan.GradleEnterpriseBuildScan
import groovy.json.JsonSlurper
import spock.lang.Unroll

abstract class AbstractGradlePluginDevelopmentTestingStrategyFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec {
    def "minimum supported version is passed to the test"(minimumGradleVersion) {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '${minimumGradleVersion}'
                }
            }
        """

        and:
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert tasks.functionalTest.systemProperties.'dev.gradleplugins.minimumGradleVersion' == '${minimumGradleVersion}'
                }
            }
        """

        expect:
        succeeds('verify')

        where:
        minimumGradleVersion << ['6.2', '2.14', '5.6.1']
    }

    def "sets coverage to defaults when nothing is specified"() {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        and:
        buildFile << '''
            gradlePlugin {
                extra {
                    minimumGradleVersion = '6.2'
                }
            }
        '''

        and:
        buildFile << '''
            tasks.register('verify') {
                doLast {
                    assert tasks.functionalTest.systemProperties.'dev.gradleplugins.gradleVersions' == 'default'
                }
            }
        '''

        expect:
        succeeds('verify')
    }

    @Unroll
    def "sets coverage tags on functional test tasks"(testingStrategy, expectedCoverageContext) {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '6.2'
                }
            }
            
            components.functionalTest {
                testingStrategy = ${GradlePluginTestingStrategyFactory.canonicalName}.${testingStrategy}
            }
        """

        and:
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert tasks.functionalTest.systemProperties.'dev.gradleplugins.gradleVersions' == '${expectedCoverageContext}'
                }
            }
        """

        expect:
        succeeds('verify')

        where:
        testingStrategy                                         | expectedCoverageContext
        'latestMinorVersions()'                                 | 'latestMinor'
        'allReleasedVersions()'                                 | 'all'
        'allReleasedVersions().includeLatestNightlyVersion()'   | 'all,latestNightly'
        'latestMinorVersions().includeLatestNightlyVersion()'   | 'latestMinor,latestNightly'
        'latestNightlyVersion()'                                | 'latestNightly'
    }

    def "runs tests for all released versions"() {
        given:
        makeSingleProject()
        componentUnderTest.withFunctionalTest().withTestingStrategySupport().writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '6.2'
                }
            }
            
            components.functionalTest {
                testingStrategy = ${GradlePluginTestingStrategyFactory.canonicalName}.allReleasedVersions()
            }
        """

        when:
        executer = new GradleEnterpriseBuildScan().apply(executer)
        succeeds('functionalTest')

        then:
        testResult.assertTestClassesExecuted('com.example.BasicPluginFunctionalTest')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [6.2]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [6.2.1]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [6.2.2]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [6.3]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestCount(4, 0, 0)
    }

    def "runs tests only for the latest minor of each major versions"() {
        given:
        makeSingleProject()
        componentUnderTest.withFunctionalTest().withTestingStrategySupport().writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '5.5'
                }
            }
            
            components.functionalTest {
                testingStrategy = ${GradlePluginTestingStrategyFactory.canonicalName}.latestMinorVersions()
            }
        """

        when:
        executer = new GradleEnterpriseBuildScan().apply(executer)
        succeeds('functionalTest')

        then:
        testResult.assertTestClassesExecuted('com.example.BasicPluginFunctionalTest')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [5.6.4]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [6.3]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestCount(2, 0, 0)
    }

    def "runs tests only for the latest nightly"() {
        given:
        makeSingleProject()
        componentUnderTest.withFunctionalTest().withTestingStrategySupport().writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '6.2'
                }
            }
            
            components.functionalTest {
                testingStrategy = ${GradlePluginTestingStrategyFactory.canonicalName}.latestNightlyVersion()
            }
        """

        when:
        executer = new GradleEnterpriseBuildScan().apply(executer)
        succeeds('functionalTest')

        then:
        testResult.assertTestClassesExecuted('com.example.BasicPluginFunctionalTest')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed("can do basic test [${latestNightlyVersion}]")
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestCount(1, 0, 0)
    }

    def "runs tests only for the minimum Gradle version"() {
        given:
        makeSingleProject()
        componentUnderTest.withFunctionalTest().withTestingStrategySupport().writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '6.2'
                }
            }
            
            components.functionalTest {
                testingStrategy = ${GradlePluginTestingStrategyFactory.canonicalName}.onlyMinimumVersion()
            }
        """

        when:
        executer = new GradleEnterpriseBuildScan().apply(executer)
        succeeds('functionalTest')

        then:
        testResult.assertTestClassesExecuted('com.example.BasicPluginFunctionalTest')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed("can do basic test [6.2]")
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestCount(1, 0, 0)
    }

    def "runs tests for the minimum Gradle version and latest nightly"() {
        given:
        makeSingleProject()
        componentUnderTest.withFunctionalTest().withTestingStrategySupport().writeToProject(testDirectory)

        and:
        buildFile << """
            gradlePlugin {
                extra {
                    minimumGradleVersion = '6.2'
                }
            }
            
            components.functionalTest {
                testingStrategy = ${GradlePluginTestingStrategyFactory.canonicalName}.onlyMinimumVersion().includeLatestNightlyVersion()
            }
        """

        when:
        executer = new GradleEnterpriseBuildScan().apply(executer)
        succeeds('functionalTest')

        then:
        testResult.assertTestClassesExecuted('com.example.BasicPluginFunctionalTest')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test [6.2]')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed("can do basic test [${latestNightlyVersion}]")
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestCount(2, 0, 0)
    }

    private String getLatestNightlyVersion() {
        return new JsonSlurper().parse(new URL('https://services.gradle.org/versions/nightly')).version
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
            }

            gradlePlugin {
                plugins {
                    hello {
                        id = '${componentUnderTest.pluginId}'
                        implementationClass = 'com.example.BasicPlugin'
                    }
                }
            }
        """
    }
}
