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

package dev.gradleplugins.api

import groovy.transform.CompileStatic
import org.gradle.api.Action;
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
import java.nio.file.Files
import java.nio.file.StandardCopyOption;

@CompileStatic
public abstract class GenerateGradleApiJar extends DefaultTask {
    @Input
    public abstract Property<String> getVersion();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @OutputFile
    abstract RegularFileProperty getOutputSourceFile();

    @Inject
    protected abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    private void doGenerate() {
        getWorkerExecutor().processIsolation().submit(GenerateGradleApiJarAction.class) { param ->
            param.getOutputFile().set(getOutputFile());
            param.getVersion().set(getVersion());
            param.outputSourceFile.set(outputSourceFile)
        }
    }

    public static abstract class GenerateGradleApiJarAction implements WorkAction<GenerateGradleApiJarParameters> {

        @Override
        public void execute() {
            File temporaryDir = Files.createTempDirectory("gradle").toFile();
            println("Working directory for ${parameters.version.get()}: ${temporaryDir.absolutePath}")

            new File(temporaryDir, "build.gradle").text = '''
buildscript {
    dependencies {
        classpath 'io.github.http-builder-ng:http-builder-ng-core:1.0.4'
    }
    repositories {
        mavenCentral()
    }
}
apply plugin: 'java'
dependencies {
compile(gradleApi())
}

import groovyx.net.http.HttpBuilder
import groovyx.net.http.optional.Download

task downloadGradleSourceJar {
    ext.outputFile = new File(temporaryDir, 'gradle-src.zip')
    inputs.property('gradleVersion', gradle.gradleVersion)
    outputs.file(outputFile)
    doLast {
        File file = HttpBuilder.configure { request.uri = "https://services.gradle.org/distributions/gradle-${gradle.gradleVersion}-src.zip" }.get {
            Download.toFile(delegate, outputFile)
        }
    }
}

task createGradleSourceJar(type: Zip) {
    dependsOn(downloadGradleSourceJar)

    from({zipTree(project.tasks.downloadGradleSourceJar.outputFile).matching { include('*/subprojects/*/src/main/**/*') } }) {
//        eachFile {
//            relativePath = new RelativePath(relativePath.file, relativePath.segments.drop(6))
//        }
    }
    baseName = 'gradle-api'
    extension = 'jar'
    classifier = 'sources'
}
'''
            new File(temporaryDir, "settings.gradle").text = '''
rootProject.name = 'gradle-api-jar'
'''
            new File(temporaryDir, "src/main/java").mkdirs();
            new File(temporaryDir, "src/main/java/Some.java").text = '''
public class Some {}
'''

            ProjectConnection connection = GradleConnector.newConnector()
                    .forProjectDirectory(temporaryDir)
                    .useGradleVersion(getParameters().getVersion().get())
                    .connect();

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            try {
                connection.newBuild().setStandardOutput(outStream).forTasks("build", "createGradleSourceJar").run();
            } finally {
                connection.close();
                println(outStream.toString())
            }

            Files.copy(new File(System.getProperty("user.home") + "/.gradle/caches/" + getParameters().getVersion().get() + "/generated-gradle-jars/gradle-api-" + getParameters().getVersion().get() + ".jar").toPath(), getParameters().getOutputFile().getAsFile().get().toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(new File(temporaryDir, "/build/distributions/gradle-api-sources.jar").toPath(), getParameters().getOutputSourceFile().getAsFile().get().toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static interface GenerateGradleApiJarParameters extends WorkParameters {
        RegularFileProperty getOutputFile();
        RegularFileProperty getOutputSourceFile();
        Property<String> getVersion();
    }
}
