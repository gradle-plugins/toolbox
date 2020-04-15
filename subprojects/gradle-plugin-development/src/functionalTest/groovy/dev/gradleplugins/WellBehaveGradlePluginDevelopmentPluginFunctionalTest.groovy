/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GradlePluginElement
import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.integtests.fixtures.ArchiveTestFixture
import org.hamcrest.CoreMatchers
import org.junit.Assume
import spock.lang.Ignore
import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.not

// TODO: Add fork Gradle Executer so I can test what user really experience once the plugin is released
abstract class WellBehaveGradlePluginDevelopmentPluginFunctionalTest extends AbstractGradlePluginDevelopmentFunctionalSpec implements ArchiveTestFixture {
    protected String getProjectName() {
        return 'gradle-plugin'
    }

    protected String configureGradlePluginExtension() {
        return """
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

    def setup() {
        settingsFile << "rootProject.name = '${projectName}'"
    }

    def "fails when java-gradle-plugin core plugin is applied before dev.gradleplugins development plugin"() {
        given:
        buildFile << """
            plugins {
                id("java-gradle-plugin")
                ${configureApplyPluginUnderTest()}
            }

            ${configureGradlePluginExtension()}
        """

        when:
        fails 'help'

        then:
        outputContains("The Gradle core plugin 'java-gradle-plugin' should not be applied within your build when using '${pluginIdUnderTest}'.")
    }

    @Unroll
    def "fails when applying #otherPluginId together with the plugin under test"() {
        Assume.assumeThat(otherPluginId, not(equalTo(pluginIdUnderTest)))

        given:
        buildFile << """
            plugins {
                id("${otherPluginId}")
                ${configureApplyPluginUnderTest()}
            }

            ${configureGradlePluginExtension()}
        """

        when:
        fails 'help'

        then:
        outputContains("The '${pluginIdUnderTest}' cannot be applied with '${otherPluginId}', please apply just one of them.")

        where:
        otherPluginId << ['dev.gradleplugins.java-gradle-plugin', 'dev.gradleplugins.groovy-gradle-plugin']
    }

    def "can build a Gradle plugin"() {
        given:
        buildFile << """
            plugins {
                ${configureApplyPluginUnderTest()}
            }

            ${configureGradlePluginExtension()}
        """
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('build')

        then:
        result.assertTaskNotSkipped(':build')
        jar("build/libs/${projectName}.jar").hasDescendants('com/example/BasicPlugin.class',"META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties")
        jar("build/libs/${projectName}.jar").assertFileContent("META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties", CoreMatchers.startsWith('implementation-class=com.example.BasicPlugin'))
    }


    // TODO: Assert the right version of the fixture is pulled
    def "can functional test a Gradle plugin"() {
        given:
        buildFile << """
            plugins {
                ${configureApplyPluginUnderTest()}
            }

            repositories {
                jcenter()
            }
            
            ${configureGradlePluginExtension()}
        """
        componentUnderTest.withFunctionalTest().writeToProject(testDirectory)

        when:
        succeeds('build')

        then:
        result.assertTaskNotSkipped(':build')
        jar("build/libs/${projectName}.jar").hasDescendants('com/example/BasicPlugin.class',"META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties")
        jar("build/libs/${projectName}.jar").assertFileContent("META-INF/gradle-plugins/${componentUnderTest.pluginId}.properties", CoreMatchers.startsWith('implementation-class=com.example.BasicPlugin'))
    }

    def "fails if `kotlin-dsl` plugin is applied"() {
        given:
        useKotlinDsl()
        buildFile << """
            plugins {
                `kotlin-dsl`
                id("${pluginIdUnderTest}")
            }
            
            ${configureGradlePluginExtension()}
        """

        when:
        def result = fails('help')

        then:
        result.output.contains("An exception occurred applying plugin request [id: '${pluginIdUnderTest}']")
        result.output.contains("Failed to apply plugin [id '${pluginIdUnderTest}']")
        result.output.contains("The Gradle plugin 'kotlin-dsl' should not be applied within your build when using '${pluginIdUnderTest}'.")
    }

    // TODO: Not a wellbehave test but more like using it in real life
    def "can use the plugin inside a composite build"() {
        def pluginDirectory = testDirectory.file("hello-gradle-plugin")
        componentUnderTest.writeToProject(pluginDirectory)
        pluginDirectory.file(buildFileName) << """
            plugins {
                ${configureApplyPluginUnderTest()}
            }
            group = "com.example"
            version = "1.0"

            // TODO: This is need for Kotlin only, find a way to isolate this only Kotlin tests
            repositories.mavenCentral()

            // HACK: We should have a way to get the "fake" snapshot jars
            repositories {
                maven {
                    url "${System.properties['user.home']}/.m2/repository"
                }
            }
            
            ${configureGradlePluginExtension()}
        """
        pluginDirectory.file(settingsFileName) << """
            rootProject.name = "hello-gradle-plugin"
        """

        buildFile << """
            plugins {
                id("${componentUnderTest.pluginId}")
            }
        """
        settingsFile.text = """
            pluginManagement {
                resolutionStrategy {
                    eachPlugin {
                        if (requested.id.id == "${componentUnderTest.pluginId}") {
                            useModule("com.example:hello-gradle-plugin:1.0")
                        }
                    }
                }
            }

            includeBuild("hello-gradle-plugin")
        """ + settingsFile.text

        when:
        run('help')

        then:
        outputContains("Hello")
    }

    def "can use published plugin through build script DSL"() {
        publishPluginUnderTest()

        buildFile << """
            buildscript {
                dependencies {
                    classpath "com.example:hello-gradle-plugin:1.0"
                }
                repositories {
                    mavenLocal()
                }
            }

            apply plugin: 'com.example.hello'
        """

        when:
        run('help')

        then:
        outputContains("Hello")
    }

    def "can use published plugin through plugin DSL"() {
        publishPluginUnderTest()
        settingsFile.text = configurePluginDslForPluginUnderTest() + settingsFile.text
        buildFile.text = """
            plugins {
                id("${componentUnderTest.pluginId}")
            }
        """

        when:
        run('help')

        then:
        outputContains('Hello')
    }

    @Ignore("This feature has being remove for now")
    def "gives a useful message when using the plugin with older Gradle distribution"() {
        publishPluginUnderTest()
        settingsFile.text = configurePluginDslForPluginUnderTest() + settingsFile.text
        buildFile.text = """
            plugins {
                id("${componentUnderTest.pluginId}")
            }
        """

        when:
        executer.usingGradleVersion("5.6")
        fails('help')

        then:
        // TODO parse exception instead once fixture supports them
        outputContains("Plugin 'com.example.hello' does not support the current version of Gradle (5.6). Please use at least Gradle 5.6.2.")
    }

    @Ignore("Test using a plugin")
    def "fails on unsupported Gradle version"() {
        given:
        buildFile << """
            plugins {
                ${configureApplyPluginUnderTest()}
            }

            ${configureGradlePluginExtension()}
        """

        when:
        executer.usingGradleVersion("5.4")
        fails("tasks")

        then:
        outputContains("...")
    }

    @Ignore("Until gradle-testkit-fixtures support changing Java runtime")
    def "fails on unsupported Java version"() {
        given:
        buildFile << """
            plugins {
                ${configureApplyPluginUnderTest()}
            }

            ${configureGradlePluginExtension()}
        """

        when:
        fails("tasks")

        then:
        outputContains("Plugin 'dev.gradleplugins.dummy' does not support the current JVM (1.8.0_171). Please use at least JVM 8.")
    }

    private void publishPluginUnderTest() {
        def pluginDirectory = testDirectory.file("hello-gradle-plugin")
        componentUnderTest.writeToProject(pluginDirectory)
        pluginDirectory.file(buildFileName) << """
            plugins {
                ${configureApplyPluginUnderTest()}
                id("maven-publish")
            }
            group = "com.example"
            version = "1.0"

            publishing.repositories {
                mavenLocal()
            }

            // HACK: We should have a way to get the "fake" snapshot jars
            repositories {
                maven {
                    url "${System.properties['user.home']}/.m2/repository"
                }
            }
            
            ${configureGradlePluginExtension()}
        """
        pluginDirectory.file(settingsFileName) << """
            rootProject.name = "hello-gradle-plugin"
        """
        using m2
        executer.inDirectory(pluginDirectory).withTasks("publish").run()
    }

    // TODO: This could maybe be move into some fixtures around publishing and using plugins...
    private String configurePluginDslForPluginUnderTest() {
        // configure plugin DSL so it "fake" the plugin portal
        return """
            pluginManagement {
                resolutionStrategy {
                    eachPlugin {
                        if (requested.id.id.equals("${componentUnderTest.pluginId}")) {
                            useModule("com.example:hello-gradle-plugin:1.0")
                        }
                    }
                }
                repositories {
                    mavenLocal()
                }
            }
        """
    }

    protected String configureApplyPluginUnderTest() {
        return """id("${pluginIdUnderTest}")"""
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract GradlePluginElement getComponentUnderTest()
}