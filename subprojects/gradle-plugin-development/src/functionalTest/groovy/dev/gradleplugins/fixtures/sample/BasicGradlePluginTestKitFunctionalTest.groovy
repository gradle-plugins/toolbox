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

import dev.gradleplugins.integtests.fixtures.AbstractFunctionalSpec
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile

class BasicGradlePluginTestKitFunctionalTest extends SourceElement {
    final List<SourceFile> files = Collections.singletonList(sourceFile('groovy', 'com/example/BasicPluginFunctionalTest.groovy', """package com.example
import ${AbstractFunctionalSpec.canonicalName}

class BasicPluginFunctionalTest extends ${AbstractFunctionalSpec.simpleName} {
    def "can do basic test"() {
        given:
        buildFile << '''
            plugins {
                id('com.example.hello')
            }
        '''

        when:
        succeeds('help')

        then:
        result.output.contains('Hello')
    }
}
"""))

    @Override
    String getSourceSetName() {
        return "functionalTest"
    }
}
