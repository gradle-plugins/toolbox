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

import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] a) throws IOException {
        if (a.length < 2) {
            throw new IllegalArgumentException("missing gradle version");
        }
        File temporaryDir = Files.createTempDirectory("gradle").toFile();
        try (PrintWriter out = new PrintWriter(new File(temporaryDir, "build.gradle"))) {
            out.println("plugins {");
            out.println("    id('java-library')");
            out.println("}");
            out.println("dependencies {");
            out.println("    implementation(gradleApi())");
            out.println("}");
//            out.println("tasks.create('bob').doLast { configurations.implementation.artifacts.files }");
        }
        try (PrintWriter out = new PrintWriter(new File(temporaryDir, "settings.gradle"))) {
            out.println("rootProject.name = 'gradle-api-jar'");
        }
        new File(temporaryDir, "src/main/java").mkdirs();
        try (PrintWriter out = new PrintWriter(new File(temporaryDir, "src/main/java/Some.java"))) {
            out.println("public class Some {}");
        }

        File gradleUserHomeDir = new File(temporaryDir, "gradle-user-home");

        List<String> args = new ArrayList<>();
        args.add("--gradle-user-home");
        args.add(gradleUserHomeDir.getAbsolutePath());
        args.add("-q");
        args.add("build");
        GradleRunner.create()
                .withProjectDir(temporaryDir)
                .withArguments(args)
                .withGradleVersion(a[0]).build();

        Files.copy(new File(gradleUserHomeDir, "caches/" + a[0] + "/generated-gradle-jars/gradle-api-" + a[0] + ".jar").toPath(), new File(a[1] + "/gradle-api-" + a[0] + ".jar").toPath());
//        System.out.println(new File(gradleUserHomeDir, "caches/" + a[0] + "/generated-gradle-jars/gradle-api-" + a[0] + ".jar").getCanonicalPath());
    }
}
