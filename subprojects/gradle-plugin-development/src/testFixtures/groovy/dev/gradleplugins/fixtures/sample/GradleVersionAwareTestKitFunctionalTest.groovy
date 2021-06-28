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

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

class GradleVersionAwareTestKitFunctionalTest extends SourceElement {
    @Override
    List<SourceFile> getFiles() {
        return Collections.singletonList(sourceFile('groovy', 'com/example/VersionAwareFunctionalTest.groovy', """package com.example
import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification
import java.nio.file.Path
import org.gradle.util.GradleVersion

class VersionAwareFunctionalTest extends Specification {
    private static final String DEFAULT_GRADLE_VERSION_SYSPROP_NAME = 'dev.gradleplugins.defaultGradleVersion'
    @TempDir Path testProjectDir
    File settingsFile
    File buildFile

    def setup() {
        settingsFile = testProjectDir.resolve('settings.gradle').toFile()
        buildFile = testProjectDir.resolve('build.gradle').toFile()

        if (System.properties.containsKey(DEFAULT_GRADLE_VERSION_SYSPROP_NAME)) {
            println "Default Gradle version: \${gradleDistributionUnderTest}"
        } else {
            println "No default Gradle version"
        }
    }

    private static String getGradleDistributionUnderTest() {
        String defaultGradleVersionUnderTest = System.getProperty(DEFAULT_GRADLE_VERSION_SYSPROP_NAME, null)
        if (defaultGradleVersionUnderTest == null) {
            return GradleVersion.current().version;
        }
        return defaultGradleVersionUnderTest
    }

    def "print gradle version from within the executor"() {
        given:
        settingsFile << "rootProject.name = 'hello-world'"
        buildFile << '''
            plugins {
                id('com.example.hello')
            }

            println "Using Gradle version: \${project.gradle.gradleVersion}"
        '''

        when:
        def result = GradleRunner.create()
            .withPluginClasspath()
            .forwardOutput()
            .withGradleVersion(gradleDistributionUnderTest)
            .withProjectDir(testProjectDir.toFile())
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
        return "functionalTest"
    }
}
