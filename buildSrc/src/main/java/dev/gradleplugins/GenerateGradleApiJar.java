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

package dev.gradleplugins;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class GenerateGradleApiJar extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();

    @Classpath
    public abstract ConfigurableFileCollection getClasspath();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    private void doGenerate() {
        getOutputFile().get().getAsFile().delete();
        getProject().javaexec(spec -> {
            spec.setMain("dev.gradleplugins.Main");
            spec.setClasspath(getClasspath());
            spec.args(getVersion().get(), getOutputFile().get().getAsFile().getParentFile().getAbsolutePath());
        });
    }
}
