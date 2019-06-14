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

package dev.gradleplugins.internal

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.GroovySourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

class SpockFunctionalTestingPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        project.pluginManager.apply("groovy-base") // For Spock testing
        configureFunctionalTestingWithSpockAndTestKit(project)
    }

    fun configureFunctionalTestingWithSpockAndTestKit(project: Project) {
        val sourceSets = project.extensions.getByType(SourceSetContainer::class.java)

        val functionalTestSourceSet = sourceSets.create("functionalTest") {
            withConvention(GroovySourceSet::class) {
                groovy.srcDir("src/functTest/groovy")
                groovy.srcDir("src/functionalTest/groovy")
            }
            resources.srcDir("src/functTest/resources")
            resources.srcDir("src/functionalTest/resources")
            compileClasspath += sourceSets.getByName("main").output
            runtimeClasspath += output + compileClasspath
        }

        project.dependencies {
            add("functionalTestImplementation", "org.spockframework:spock-core:1.2-groovy-2.5") {
                exclude(group = "org.codehaus.groovy")
            }
            add("functionalTestImplementation", gradleTestKit())

            // TODO: add current plugin artifact
        }

        val functionalTest = project.tasks.register("functionalTest", Test::class) {
            description = "Runs the functional tests"
            group = "verification"
            testClassesDirs = functionalTestSourceSet.output.classesDirs
            classpath = functionalTestSourceSet.runtimeClasspath
            mustRunAfter("test")
        }

        project.tasks.named("check") { dependsOn(functionalTest) }
    }
}