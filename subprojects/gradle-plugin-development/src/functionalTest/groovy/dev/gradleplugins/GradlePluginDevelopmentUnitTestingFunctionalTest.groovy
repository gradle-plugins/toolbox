package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import dev.gradleplugins.integtests.fixtures.ArchiveTestFixture
import dev.gradleplugins.test.fixtures.scan.GradleEnterpriseBuildScan
import dev.gradleplugins.test.fixtures.sources.SourceElement
import org.hamcrest.CoreMatchers

abstract class AbstractGradlePluginDevelopmentUnitTestingFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec implements ArchiveTestFixture {
    def "can unit test a Gradle plugin"() {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        executer = new GradleEnterpriseBuildScan().apply(executer)
        succeeds('build')

        then:
        result.assertTasksExecuted(allTasksToBuild)
        result.assertTaskNotSkipped(':test')

        and:
        testResult.assertTestClassesExecuted('com.example.BasicPluginTest')
        testResult.testClass('com.example.BasicPluginTest').assertTestPassed('can do basic test')
        testResult.testClass('com.example.BasicPluginTest').assertTestCount(1, 0, 0)

        and:
        jar("build/libs/gradle-plugin.jar").hasDescendants('com/example/BasicPlugin.class',"META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties")
        jar("build/libs/gradle-plugin.jar").assertFileContent("META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties", CoreMatchers.startsWith('implementation-class=com.example.BasicPlugin'))
    }

    protected DefaultTestExecutionResult getTestResult() {
        new DefaultTestExecutionResult(testDirectory, 'build', '', '', 'test')
    }

    protected List<String> getAllTasksToBuild() {
        return [':compileJava', ':compileGroovy', ':pluginDescriptors', ':processResources', ':classes', ':jar', ':assemble', ':validatePlugins', ':check', ':build'] + testTasks
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
                id 'dev.gradleplugins.gradle-plugin-unit-test'
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
            
            import static ${GradleRuntimeCompatibility.canonicalName}.groovyVersionOf
            
            test {
                dependencies {
                    implementation spockFramework()
                    implementation groovy()
                }
            }
        """
    }
}

class GroovyGradlePluginDevelopmentUnitTestingFunctionalTest extends AbstractGradlePluginDevelopmentUnitTestingFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected SourceElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin().withProjectBuilderTest()
    }
}

class JavaGradlePluginDevelopmentUnitTestingFunctionalTest extends AbstractGradlePluginDevelopmentUnitTestingFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected SourceElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin().withProjectBuilderTest()
    }
}
