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

plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_6
    targetCompatibility = JavaVersion.VERSION_1_6
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("dev.gradleplugins:gradle-api:0.0.13-3.5.1")
}

configurations {
    create("stubElements") {
        isVisible = false
        isCanBeResolved = false
        isCanBeConsumed = true
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "stub"))
        outgoing.artifact(project.layout.buildDirectory.dir("classes/java/main")) {
            builtBy(tasks.named("compileJava"))
        }
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.debugOptions.debugLevel = "none"
}