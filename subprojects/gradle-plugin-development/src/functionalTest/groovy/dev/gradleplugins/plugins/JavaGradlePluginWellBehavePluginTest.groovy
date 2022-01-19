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

package dev.gradleplugins.plugins

import dev.gradleplugins.integtests.fixtures.WellBehavedPluginTest

class JavaGradlePluginWellBehavePluginTest extends WellBehavedPluginTest {
    final String qualifiedPluginId = "dev.gradleplugins.java-gradle-plugin"


    private static final Set<String> PUBLISH_PLUGIN_REALIZED_TASKS = [':login', ':publishPlugins', ':publishPluginJar', ':publishPluginJavaDocsJar', ':javadoc', ':jar']
    Set<String> getRealizedTaskPaths() {
        return PUBLISH_PLUGIN_REALIZED_TASKS + super.getRealizedTaskPaths()
    }
}
