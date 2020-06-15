package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin
import dev.gradleplugins.fixtures.test.DefaultTestExecutionResult
import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.sources.SourceElement

abstract class AbstractGradlePluginDevelopmentTestKitFunctionalTest extends AbstractGradleSpecification {
    def "can execute vanilla TestKit tests"() {
        given:
        makeSingleProject()
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('test')

        then:
        result.assertTasksExecuted(':compileJava', ':compileGroovy', ':pluginDescriptors', ':processResources', ':classes', ':pluginUnderTestMetadata', ':compileTestJava', ':compileTestGroovy', ':processTestResources', ':testClasses', ':test')
        new DefaultTestExecutionResult(testDirectory).testClass('com.example.BasicPluginFunctionalTest').assertTestPassed('can do basic test')
    }

    protected void makeSingleProject() {
        settingsFile << "rootProject.name = 'gradle-plugin'"
        buildFile << """
            plugins {
                id '${pluginIdUnderTest}'
                id 'groovy-base'
            }

            gradlePlugin {
                testSourceSets sourceSets.test
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
            
            dependencies {
                testImplementation platform('org.spockframework:spock-bom:1.2-groovy-2.5')
                testImplementation 'org.spockframework:spock-core'
                testImplementation gradleTestKit()
            }
        """
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract SourceElement getComponentUnderTest()
}

class GroovyGradlePluginDevelopmentTestKitFunctionalTest extends AbstractGradlePluginDevelopmentTestKitFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected SourceElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin().withTestKitTest()
    }
}

class JavaGradlePluginDevelopmentTestKitFunctionalTest extends AbstractGradlePluginDevelopmentTestKitFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected SourceElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin().withTestKitTest()
    }
}