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

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceFile
import org.gradle.api.Plugin
import org.gradle.api.Project

class JavaBasicGradlePlugin extends GradlePluginElement {
    final String pluginId = "com.example.hello"
    final List<SourceFile> files = Collections.singletonList(sourceFile('java', 'com/example/BasicPlugin.java', """package com.example;
import ${Plugin.canonicalName};
import ${Project.canonicalName};

public class BasicPlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getLogger().lifecycle("Hello");
    }
}
"""))

    @Override
    GradlePluginElement withFunctionalTest() {
        def delegate = ofElements(this, new BasicGradlePluginTestKitFunctionalTest())
        return new GradlePluginElement() {
            @Override
            String getPluginId() {
                return JavaBasicGradlePlugin.this.pluginId
            }

            @Override
            GradlePluginElement withFunctionalTest() {
                return this
            }

            @Override
            List<SourceFile> getFiles() {
                throw new UnsupportedOperationException()
            }

            @Override
            void writeToProject(TestFile projectDir) {
                delegate.writeToProject(projectDir)
            }
        }
    }
}
