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

package dev.gradleplugins.api;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;

public abstract class GenerateGradleApiJar extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    private void doGenerate() {
        getWorkerExecutor().noIsolation().submit(GenerateGradleApiJarAction.class, param -> {
            param.getOutputFile().set(getOutputFile());
            param.getVersion().set(getVersion());
        });

    }

    public static abstract class GenerateGradleApiJarAction implements WorkAction<GenerateGradleApiJarParameters> {

        @Override
        public void execute() {
            getParameters().getOutputFile().get().getAsFile().delete();

            try {
                File temporaryDir = Files.createTempDirectory("gradle").toFile();
                try (PrintWriter out = new PrintWriter(new File(temporaryDir, "build.gradle"))) {
                    out.println("apply plugin: 'java'");
                    out.println("dependencies {");
                    out.println("    compile(gradleApi())");
                    out.println("}");
                }
                try (PrintWriter out = new PrintWriter(new File(temporaryDir, "settings.gradle"))) {
                    out.println("rootProject.name = 'gradle-api-jar'");
                }
                new File(temporaryDir, "src/main/java").mkdirs();
                try (PrintWriter out = new PrintWriter(new File(temporaryDir, "src/main/java/Some.java"))) {
                    out.println("public class Some {}");
                }

                ProjectConnection connection = GradleConnector.newConnector()
                        .forProjectDirectory(temporaryDir)
                        .useGradleVersion(getParameters().getVersion().get())
                        .connect();

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                try {
                    connection.newBuild().setStandardOutput(outStream).forTasks("build").run();
                } finally {
                    connection.close();
                }

                Files.copy(new File(System.getProperty("user.home") + "/.gradle/caches/" + getParameters().getVersion().get() + "/generated-gradle-jars/gradle-api-" + getParameters().getVersion().get() + ".jar").toPath(), getParameters().getOutputFile().getAsFile().get().toPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public interface GenerateGradleApiJarParameters extends WorkParameters {
        RegularFileProperty getOutputFile();
        Property<String> getVersion();
    }
}
