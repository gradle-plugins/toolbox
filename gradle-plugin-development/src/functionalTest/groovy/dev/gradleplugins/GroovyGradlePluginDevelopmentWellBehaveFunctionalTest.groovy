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

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
import dev.gradleplugins.fixtures.SourceElement

class GroovyGradlePluginDevelopmentWellBehaveFunctionalTest extends WellBehaveGradlePluginDevelopmentPluginFunctionalTest {
    final String pluginIdUnderTest = 'dev.gradleplugins.groovy-gradle-plugin'

    @Override
    protected SourceElement getComponentUnderTest() {
        return new GroovyBasicGradlePlugin()
    }

    // Strange test case not working with the dev.gradleplugins.* plugins
    def 'bb'() {
        // IT seems the code can't execute properly inside TestKit...
        buildFile << """
plugins {
    // Apply the groovy plugin to add support for Groovy
    id 'groovy'
    id 'java-gradle-plugin'
    id 'com.gradle.plugin-publish' version '0.10.1'
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // Use the latest Groovy version for building this library
    implementation 'org.codehaus.groovy:groovy-all:2.5.4'

    // Use the awesome Spock testing and specification framework
    testImplementation 'org.spockframework:spock-core:1.3-groovy-2.5'
}
"""
        getComponentUnderTest().writeToProject(testDirectory)

        expect:
        succeeds("build")
    }
}
