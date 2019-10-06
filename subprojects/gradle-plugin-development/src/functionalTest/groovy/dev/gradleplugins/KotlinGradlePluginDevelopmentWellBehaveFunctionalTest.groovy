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

import dev.gradleplugins.fixtures.sample.KotlinBasicGradlePlugin
import dev.gradleplugins.fixtures.SourceElement

class KotlinGradlePluginDevelopmentWellBehaveFunctionalTest extends WellBehaveGradlePluginDevelopmentPluginFunctionalTest {
    final String pluginIdUnderTest = 'dev.gradleplugins.kotlin-gradle-plugin'

    @Override
    protected String configureApplyPluginUnderTest() {
        return super.configureApplyPluginUnderTest() + """
id("org.jetbrains.kotlin.jvm") version "1.3.50"
"""
    }

    @Override
    protected SourceElement getComponentUnderTest() {
        return new KotlinBasicGradlePlugin()
    }

    def "fails with an helpful message when org.jetbrains.kotlin.jvm"() {
        given:
        buildFile << """
            plugins {
                id("dev.gradleplugins.kotlin-gradle-plugin")
            }
        """

        when:
        def result1 = fails('help')

        then:
        result1.output.contains("You need to manually apply the `org.jetbrains.kotlin.jvm` plugin inside the plugin block:")

        when:
        buildFile.text = buildFile.text.replace('id("dev.gradleplugins.kotlin-gradle-plugin")', '''id("dev.gradleplugins.kotlin-gradle-plugin")\nid("org.jetbrains.kotlin.jvm") version "1.3.50"''')
        def result2 = succeeds('help')

        then:
        result2.output.contains("BUILD SUCCESSFUL")
    }
}
