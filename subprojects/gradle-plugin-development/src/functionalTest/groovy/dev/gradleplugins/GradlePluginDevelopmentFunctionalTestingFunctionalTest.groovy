package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import dev.gradleplugins.integtests.fixtures.ArchiveTestFixture
import org.hamcrest.CoreMatchers

abstract class AbstractGradlePluginDevelopmentFunctionalTestingFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec implements ArchiveTestFixture {
    // TODO: Assert the right version of the fixture is pulled
    def "can functional test a Gradle plugin"() {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('build')

        then:
        result.assertTasksExecuted(allTasksToBuild)
        result.assertTaskSkipped(':test')
        result.assertTaskNotSkipped(':functionalTest')

        and:
        testResult.assertTestClassesExecuted('com.example.BasicPluginFunctionalTest')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test')
        testResult.testClass('com.example.BasicPluginFunctionalTest').assertTestCount(1, 0, 0)

        and:
        jar("build/libs/gradle-plugin.jar").hasDescendants('com/example/BasicPlugin.class',"META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties")
        jar("build/libs/gradle-plugin.jar").assertFileContent("META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties", CoreMatchers.startsWith('implementation-class=com.example.BasicPlugin'))
    }

    protected DefaultTestExecutionResult getTestResult() {
        new DefaultTestExecutionResult(testDirectory, 'build', '', '', 'functionalTest')
    }

    protected List<String> getAllTasksToBuild() {
        return [':compileJava', ':compileGroovy', ':pluginDescriptors', ':processResources', ':classes', ':jar', ':assemble', ':pluginUnderTestMetadata', ':validatePlugins', ':check', ':build'] + functionalTestTasks + testTasks
    }

    protected List<String> getFunctionalTestTasks() {
        return [':compileFunctionalTestJava', ':compileFunctionalTestGroovy', ':processFunctionalTestResources', ':functionalTestClasses', ':functionalTest']
    }

    protected List<String> getTestTasks() {
        return [':compileTestJava', ':compileTestGroovy', ':processTestResources', ':testClasses', ':test']
    }

    protected abstract SourceElement getComponentUnderTest()

    protected abstract String getPluginIdUnderTest()

    protected void makeSingleProject() {
        settingsFile << 'rootProject.name = "gradle-plugin"'
        buildFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-functional-test'
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
            
            repositories {
                mavenCentral()
            }
            
            functionalTest {
                dependencies {
                    implementation spockFramework()
                    implementation gradleFixtures()
                }
            }
        """
    }
}

class GroovyGradlePluginDevelopmentFunctionalTestingFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalTestingFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected SourceElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin().withFunctionalTest()
    }
}

class JavaGradlePluginDevelopmentFunctionalTestingFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalTestingFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected SourceElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin().withFunctionalTest()
    }
}
