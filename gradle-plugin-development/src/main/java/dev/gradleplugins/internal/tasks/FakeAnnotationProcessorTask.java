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

package dev.gradleplugins.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public abstract class FakeAnnotationProcessorTask extends DefaultTask {
    public abstract ConfigurableFileCollection getSource();
    public abstract DirectoryProperty getPluginDescriptorDirectory();

    @TaskAction
    private void doGenerate() throws FileNotFoundException {
        writePluginDescriptor("com.example.hello", "com.example.BasicPlugin");
    }

    private void writePluginDescriptor(String pluginId, String pluginClass) throws FileNotFoundException {
        File pluginDescriptorFile = getPluginDescriptorDirectory().file(pluginId + ".properties").get().getAsFile();
        pluginDescriptorFile.getParentFile().mkdirs();
        try (PrintWriter out = new PrintWriter(pluginDescriptorFile)) {
            out.println("implementation-class=" + pluginClass);
        }
    }
}
