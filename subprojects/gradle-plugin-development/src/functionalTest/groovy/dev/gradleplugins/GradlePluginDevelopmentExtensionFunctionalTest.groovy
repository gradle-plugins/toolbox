package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin
import spock.lang.Unroll
import spock.util.environment.Jvm

abstract class AbstractGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec {
    def "register an compatibility extension on gradlePlugin extension"() {
        given:
        makeSingleProject()
        buildFile << """
            tasks.register('verify') {
                doLast {
                    assert gradlePlugin.compatibility instanceof ${GradlePluginDevelopmentCompatibilityExtension.canonicalName}
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
                    assert gradlePlugin.compatibility.minimumGradleVersion.get() == project.gradle.gradleVersion
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
            gradlePlugin.compatibility.minimumGradleVersion = '${gradleVersion}'
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

    def "cannot change minimum Gradle version after configured"() {
        given:
        makeSingleProject()
        buildFile << """
            afterEvaluate {
                gradlePlugin.compatibility.minimumGradleVersion = '6.2'
            }
        """

        expect:
        fails('help')
        failure.assertHasDescription("A problem occurred configuring root project 'gradle-plugin'.")
        failure.assertHasCause("The value for property 'minimumGradleVersion' cannot be changed any further.")
    }

    def "can generate Javadoc Jar"() {
        given:
        makeSingleProject()
        buildFile << """
            gradlePlugin.extra {
                withJavadocJar()
            }
        """

        expect:
        succeeds('javadocJar')
    }

    def "can generate source Jar"() {
        given:
        makeSingleProject()
        buildFile << """
            gradlePlugin.extra {
                withSourcesJar()
            }
        """

        expect:
        succeeds('sourcesJar')
    }

    def "can access compatibility extension from Kotlin DSL"() {
        given:
        useKotlinDsl()
        makeSingleProject()
        buildFile << """
            gradlePlugin.compatibility.minimumGradleVersion.set("2.14")
            gradlePlugin.compatibility {
                minimumGradleVersion.set("2.14")
            }
            gradlePlugin {
                compatibility.minimumGradleVersion.set("2.14")
                compatibility {
                    minimumGradleVersion.set("2.14")
                }
            }

            tasks.register("verify") {
                doLast {
                    assert(gradlePlugin.compatibility.minimumGradleVersion.get() == "2.14")
                }
            }
        """

        expect:
        succeeds('verify')
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract GradlePluginElement getComponentUnderTest()

    protected void makeSingleProject() {
        // NOTE: The project is written to be Kotlin/Groovy DSL compatible
        settingsFile << 'rootProject.name = "gradle-plugin"'
        buildFile << """
            plugins {
                id("${pluginIdUnderTest}")
            }

            gradlePlugin {
                plugins.create("hello") {
                    id = "${componentUnderTest.pluginId}"
                    implementationClass = "com.example.BasicPlugin"
                }
            }
        """
    }
}

class GroovyGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentExtensionFunctionalTest implements GroovyGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin()
    }
}

class JavaGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentExtensionFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin()
    }
}