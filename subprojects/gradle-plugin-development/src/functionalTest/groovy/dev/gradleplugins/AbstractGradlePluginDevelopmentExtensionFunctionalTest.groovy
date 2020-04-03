package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import spock.lang.Unroll
import spock.util.environment.Jvm

abstract class AbstractGradlePluginDevelopmentExtensionFunctionalTest extends AbstractFunctionalSpec {
    def "register an extra extension on gradlePlugin extension"() {
        given:
        makeSingleProject()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert gradlePlugin.extra instanceof ${extraExtensionClass.canonicalName}
                }
            }
        """

        expect:
        succeeds('verify')
    }

    def "configure the minimum Gradle version to current Gradle version"() {
        given:
        makeSingleProject()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert gradlePlugin.extra.minimumGradleVersion.get() == project.gradle.gradleVersion
                }
            }
        """

        expect:
        succeeds('verify')
    }

    def "does not change source/target compatibility if minimum Gradle version is not configured"() {
        given:
        makeSingleProject()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert java.sourceCompatibility.toString() == '${Jvm.current.javaSpecificationVersion}'
                    assert java.targetCompatibility.toString() == '${Jvm.current.javaSpecificationVersion}'
                }
            }
        """

        expect:
        succeeds('verify')
    }

    @Unroll
    def "changes source/target compatibility to the minimum Gradle version configured"() {
        given:
        makeSingleProject()
        buildFile << """
            gradlePlugin.extra.minimumGradleVersion = '${gradleVersion}'
            tasks.register('verify') {
                doLast {
                    assert java.sourceCompatibility.toString() == '${javaVersion}'
                    assert java.targetCompatibility.toString() == '${javaVersion}'
                }
            }
        """

        expect:
        succeeds('verify')

        where:
        gradleVersion   | javaVersion
        '6.2'           | '1.8'
        '5.1'           | '1.8'
        '4.3'           | '1.7'
        '3.0'           | '1.7'
        '2.14'          | '1.6'
        '1.12'          | '1.5'
    }

    protected abstract Class<?> getExtraExtensionClass()

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
