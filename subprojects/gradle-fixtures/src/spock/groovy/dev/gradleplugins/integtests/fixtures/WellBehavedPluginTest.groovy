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

package dev.gradleplugins.integtests.fixtures

import org.gradle.util.GradleVersion

import java.util.function.Supplier

abstract class WellBehavedPluginTest extends AbstractGradleSpecification {
    // TODO: Maybe we can infer this value off the environment.
    //   If we do, we should also provide an good error message telling the user what they should do if we don't infer the plugin id correctly.
    abstract String getQualifiedPluginId()

    String getMainTask() {
        return "assemble"
    }

    def "plugin does not force creation of build dir during configuration"() {
        given:
        applyPlugin()

        when:
        run "tasks"

        then:
        !file("build").exists()
    }

    def "plugin can build with empty project"() {
        given:
        applyPlugin()

        expect:
        succeeds mainTask
    }

    protected applyPlugin(File target = buildFile) {
        target << """
            plugins {
                id '${getQualifiedPluginId()}'
            }
        """
    }

    def "does not realize all possible tasks"() {
        applyPlugin()

        buildFile << """
            def configuredTasks = []
            tasks.configureEach {
                configuredTasks << it
            }
            
            gradle.buildFinished {
                def configuredTaskPaths = configuredTasks*.path
                
                // TODO: Log warning if getRealizedTaskPaths() is different than ':help'
                configuredTaskPaths.removeAll([${realizedTaskPaths.collect {"'$it'"}.join(", ")}])
                assert configuredTaskPaths == []
            }
        """
        expect:
        succeeds("help")
    }

    Set<String> getRealizedTaskPaths() {
        GradleVersion distributionUnderTest = Optional.ofNullable(System.getProperty('dev.gradleplugins.defaultGradleVersion', null)).map({ GradleVersion.version(it) }).orElseGet({ GradleVersion.current() })
        if (GradleVersion.version('7.3') > distributionUnderTest) {
            return [':help']
        } else {
            return [':help', ':clean'] // see https://github.com/gradle/gradle/issues/18214
        }
    }
}