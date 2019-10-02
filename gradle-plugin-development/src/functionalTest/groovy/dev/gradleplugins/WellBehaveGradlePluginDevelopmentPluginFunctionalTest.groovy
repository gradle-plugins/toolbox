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
import spock.lang.Unroll

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.not

abstract class WellBehaveGradlePluginDevelopmentPluginFunctionalTest extends AbstractFunctionalSpec {
    def "warns when java-gradle-plugin core plugin is applied before dev.gradleplugins development plugin"() {
        given:
        buildFile << """
            plugins {
                id("java-gradle-plugin")
                id("${pluginIdUnderTest}")
            }
        """

        when:
        run 'help', '--warn'

        then:
        outputContains("The Gradle core plugin 'java-gradle-plugin' should not be applied within your build when using '${pluginIdUnderTest}'.")
    }

    @Unroll
    def "warns when applying #otherPluginId together with the plugin under test"() {
        Assume.assumeThat(otherPluginId, not(equalTo(pluginIdUnderTest)))

        given:
        buildFile << """
            plugins {
                id("${otherPluginId}")
                id("${pluginIdUnderTest}")
            }
        """

        when:
        run 'help', '--warn'

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
        // TODO: Valid the plugin is proper
    }

    protected abstract String getPluginIdUnderTest()

    protected abstract SourceElement getComponentUnderTest()
}