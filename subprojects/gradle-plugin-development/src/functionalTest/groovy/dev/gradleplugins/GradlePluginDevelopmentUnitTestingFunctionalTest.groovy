package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import dev.gradleplugins.integtests.fixtures.ArchiveTestFixture
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal
import org.hamcrest.CoreMatchers

abstract class AbstractGradlePluginDevelopmentUnitTestingFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec implements ArchiveTestFixture {
    def "can unit test a Gradle plugin"() {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('build')

        then:
        result.assertTaskNotSkipped(':test')

        and:
        testResult.assertTestClassesExecuted('com.example.BasicPluginTest')
        testResult.testClass('com.example.BasicPluginTest').assertTestPassed('can do basic test')
        testResult.testClass('com.example.BasicPluginTest').assertTestCount(1, 0, 0)

        and:
        jar("build/libs/gradle-plugin.jar").hasDescendants('com/example/BasicPlugin.class',"META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties")
        jar("build/libs/gradle-plugin.jar").assertFileContent("META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties", CoreMatchers.startsWith('implementation-class=com.example.BasicPlugin'))
    }

    def "has no self-resolving Gradle TestKit dependency"() {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert !configurations.testImplementation.dependencies.any { it instanceof ${SelfResolvingDependencyInternal.canonicalName} ? it.targetComponentId.displayName == 'Gradle TestKit' : false }
                }
            }
        """

        expect:
        succeeds('verify')
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
                id 'groovy-base' // for spock framework
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
                    implementation platform('org.spockframework:spock-bom:2.0-groovy-3.0')
                    implementation 'org.spockframework:spock-core'
                }

                // Because spock framework 2.0 use JUnit 5
                testTasks.configureEach { useJUnitPlatform() }
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
