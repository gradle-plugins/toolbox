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

import dev.gradleplugins.fixtures.SourceElement
import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import org.junit.Assume
import spock.lang.Ignore
import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.not

abstract class WellBehaveGradlePluginDevelopmentPluginFunctionalTest extends AbstractFunctionalSpec {
    def setup() {
        settingsFile << "rootProject.name = 'root'"
    }
    def "fails when java-gradle-plugin core plugin is applied before dev.gradleplugins development plugin"() {
        given:
        buildFile << """
            plugins {
                id("java-gradle-plugin")
                id("${pluginIdUnderTest}")
            }
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
                id("${pluginIdUnderTest}")
            }
        """

        when:
        fails 'help'

        then:
        outputContains("The '${pluginIdUnderTest}' cannot be applied with '${otherPluginId}', please apply just one of them.")

        where:
        otherPluginId << ['dev.gradleplugins.java-gradle-plugin', 'dev.gradleplugins.groovy-gradle-plugin', 'dev.gradleplugins.kotlin-gradle-plugin']
    }

    def "can build Gradle plugin"() {
        given:
        buildFile << """
            plugins {
                id("${pluginIdUnderTest}")
            }
        """
        componentUnderTest.writeToProject(testDirectory)

        when:
        succeeds('build')

        then:
        assertTasksExecutedAndNotSkipped(':build')
        // TODO: Assert jar content instead
        !outputContains("No valid plugin descriptors were found in META-INF/gradle-plugins")
        !outputContains("A valid plugin descriptor was found for com.example.hello.properties but the implementation class com.example.BasicPlugin was not found in the jar.")
        // TODO: Valid the plugin is proper
    }

    @Ignore("Test using a plugin")
    def "fails on unsupported Gradle version"() {
        given:
        buildFile << """
            plugins {
                id("${pluginIdUnderTest}")
            }
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
                id("${pluginIdUnderTest}")
            }
        """

        when:
        fails("tasks")

        then:
        outputContains("Plugin 'dev.gradleplugins.dummy' does not support the current JVM (1.8.0_171). Please use at least JVM 8.")
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract SourceElement getComponentUnderTest()
}