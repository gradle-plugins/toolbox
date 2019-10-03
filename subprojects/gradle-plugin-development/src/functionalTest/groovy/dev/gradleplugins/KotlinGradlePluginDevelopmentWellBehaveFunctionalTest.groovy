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

    def setup() {
        // TODO: Can Gradle plugin be developed in Kotlin with Groovy DSL?... well it can but you can't use kotlin-dsl plugin which is fine because I don't like it.
        useKotlinDsl()
        // On Groovy DSL: id "org.jetbrains.kotlin.jvm" version "1.3.50"
        executer.beforeExecute {
            // TODO: Provide a better model for applying plugins in a DSL agnostic way.
            buildFile.text = buildFile.text.replace('id("java-gradle-plugin")', '`java-gradle-plugin`').replace("""id("${pluginIdUnderTest}")""", """id("${pluginIdUnderTest}")
    kotlin("jvm") version "1.3.50"
""")
        }
    }

    @Override
    protected SourceElement getComponentUnderTest() {
        return new KotlinBasicGradlePlugin()
    }
}
