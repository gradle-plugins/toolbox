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

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile

class GradleVersionAwareTestKitFunctionalTest extends SourceElement {
    @Override
    List<SourceFile> getFiles() {
        return Collections.singletonList(sourceFile('groovy', 'com/example/VersionAwareFunctionalTest.groovy', """package com.example
import ${AbstractGradleSpecification.canonicalName}

class VersionAwareFunctionalTest extends ${AbstractGradleSpecification.simpleName} {
    def setup() {
        if (gradleDistributionUnderTest == null) {
            println "No default Gradle version"
        } else {
            println "Default Gradle version: \${gradleDistributionUnderTest.version.version.toString()}"
        }
    }

    def "print gradle version from within the executor"() {
        given:
        buildFile << '''
            plugins {
                id('com.example.hello')
            }
            
            println "Using Gradle version: \${project.gradle.gradleVersion}"
        '''

        when:
        succeeds('help')

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
