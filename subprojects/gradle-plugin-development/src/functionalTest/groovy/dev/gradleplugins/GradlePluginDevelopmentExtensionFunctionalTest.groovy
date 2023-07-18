package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin

abstract class AbstractGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec {
    def "can generate Javadoc Jar"() {
        given:
        makeSingleProject()
        buildFile << """
            gradlePlugin.${languageName} {
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
            gradlePlugin.${languageName} {
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

    def "can disable automatic repository configuration on extension"() {
        given:
        disableLocalRepositoryInjection()
        makeSingleProject()
        buildFile << """
            gradlePlugin.${languageName} {
                disableDefaultRepositories()
            }

            tasks.register('verify') {
                doLast {
                    assert repositories*.name.size() == 0
                }
            }
        """

        expect:
        succeeds('verify')
    }

    def "can disable automatic repository configuration via Gradle properties"() {
        given:
        disableLocalRepositoryInjection()
        makeSingleProject()
        buildFile << '''
            tasks.register('verify') {
                doLast {
                    assert repositories*.name.size() == 0
                }
            }
        '''

        expect:
        succeeds('verify', '-Ddev.gradleplugins.default-repositories=disabled')
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract GradlePluginElement getComponentUnderTest()

    protected abstract String getLanguageName()

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

    @Override
    protected String getLanguageName() {
        return 'groovy'
    }

    def "can generate Groovydoc Jar"() {
        given:
        makeSingleProject()
        buildFile << """
            gradlePlugin.groovy {
                withGroovydocJar()
            }
        """

        expect:
        succeeds('groovydocJar')
    }

    def "adds default repositories to resolve key artifacts"() {
        given:
        disableLocalRepositoryInjection()
        makeSingleProject()
        buildFile << '''
            tasks.register('verify') {
                doLast {
                    assert repositories*.name == ['Gradle Plugin Development']
                }
            }
        '''

        expect:
        succeeds('verify')
    }
}

class JavaGradlePluginDevelopmentExtensionFunctionalTest extends AbstractGradlePluginDevelopmentExtensionFunctionalTest implements JavaGradlePluginDevelopmentPlugin {
    @Override
    protected GradlePluginElement getComponentUnderTest() {
        return new JavaBasicGradlePlugin()
    }

    @Override
    protected String getLanguageName() {
        return 'java'
    }


    def "adds default repositories to resolve key artifacts"() {
        given:
        disableLocalRepositoryInjection()
        makeSingleProject()
        buildFile << '''
            tasks.register('verify') {
                doLast {
                    assert repositories*.name == ['Gradle Plugin Development']
                }
            }
        '''

        expect:
        succeeds('verify')
    }
}

