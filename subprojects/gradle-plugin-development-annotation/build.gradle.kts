import com.jfrog.bintray.gradle.BintrayExtension

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
    `maven-publish`
    dev.gradleplugins.experimental.artifacts
    dev.gradleplugins.experimental.publishing
}

configure<PublishingExtension> {
    publications {
        named<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

afterEvaluate {
    configure<BintrayExtension> {
        pkg(closureOf<BintrayExtension.PackageConfig> {
            name = "plugin-development"
        })
    }
}
