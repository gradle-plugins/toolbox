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

package dev.gradleplugins.fixtures.sample

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class BasicGradlePluginTestKitTest extends SourceElement {
    private final String sourceSetName

    BasicGradlePluginTestKitTest(String sourceSetName = 'test') {
        this.sourceSetName = sourceSetName
    }

    @Override
    List<SourceFile> getFiles() {
        return Collections.singletonList(sourceFile('groovy', 'com/example/BasicPluginTest.groovy', """package com.example

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import ${Rule.canonicalName}
import ${TemporaryFolder.canonicalName}
import ${Specification.canonicalName}

class BasicPluginFunctionalTest extends ${Specification.simpleName} {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File settingsFile
    File buildFile

    def setup() {
        settingsFile = testProjectDir.newFile('settings.gradle')
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def "can do basic test"() {
        given:
        settingsFile << "rootProject.name = 'hello-world'"
        buildFile << '''
            plugins {
                id('com.example.hello')
            }
        '''

        when:
        def result = GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(testProjectDir.root)
            .withArguments('help')
            .build()

        then:
        result.output.contains('Hello')
    }
}
"""))
    }

    @Override
    String getSourceSetName() {
        return sourceSetName
    }
}
