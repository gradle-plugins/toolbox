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
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.jfrog.bintray.gradle.BintrayExtension
import dev.gradleplugins.GenerateGradleApiJar
import org.gradle.util.VersionNumber
import java.net.URL

plugins {
    `java`
    `application`
    id("com.jfrog.bintray")
    `maven-publish`
    dev.gradleplugins.experimental.`publishing-base`
}

version = "0.0.12"

val java = project.extensions.getByType(JavaPluginExtension::class.java)
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation(gradleTestKit())
}

application {
    mainClassName = "dev.gradleplugins.Main"
}

private
data class VersionDownloadInfo(val version: String, val downloadUrl: String, val snapshot: Boolean)

fun getAllGeneralAvailableVersion(): List<String> {
    val jsonText = URL("https://services.gradle.org/versions/all").readText()
    val type = object : TypeToken<List<VersionDownloadInfo>>() { }.type
    val versionInfo = Gson().fromJson<List<VersionDownloadInfo>>(jsonText, type)
    return versionInfo.filter { !it.snapshot && !it.version.contains("-rc-") && VersionNumber.parse("5.5.1").compareTo(VersionNumber.parse(it.version)) <= 0 }.map { it.version }
}

getAllGeneralAvailableVersion().forEach {
    val generateGradleApiJarTask = tasks.register<GenerateGradleApiJar>("generateGradleApi${it}") {
        getClasspath().from(tasks.getByName<JavaExec>("run").classpath)
        getVersion().set(it)
        getOutputFile().set(layout.buildDirectory.file("generated-gradle-jars/gradle-api-${it}.jar"))
    }

    val jarTask = tasks.register<Jar>("hack${it}") {
        dependsOn(generateGradleApiJarTask)
        destinationDirectory.set(layout.buildDirectory.dir("generated-gradle-jars/"))
        archiveExtension.set("jar")
        archiveBaseName.set("gradle-api")
        archiveVersion.set(it)
        enabled = false
    }

    configure<PublishingExtension> {
        publications {
            val mavenPublication = create<MavenPublication>("gradle-api-${it}") {
                artifact(jarTask.get())

                pom {
                    name.set("Gradle API v${it}")
                    description.set(project.provider { project.description })
//                    inceptionYear.set("2019")

                    developers {
                        developer {
                            id.set("gradle")
                            name.set("Gradle Inc.")
                            url.set("https://github.com/gradle")
                        }
                    }
                }
            }

            afterEvaluate {
                mavenPublication.groupId = "${project.group}"
                if (project.version.toString().endsWith("-SNAPSHOT")) {
                    val v = project.version.toString().replace("-SNAPSHOT", "")
                    mavenPublication.version = "${v}-${it}-SNAPSHOT"
                } else {
                    mavenPublication.version = "${project.version}-${it}"
                }
            }

            configure<BintrayExtension> {
                val list = if (publications != null) mutableListOf(*publications) else mutableListOf()
                list.add("gradle-api-${it}")
                setPublications(*list.toTypedArray())
            }
        }
    }
}

afterEvaluate {
    configure<BintrayExtension> {
        pkg(closureOf<BintrayExtension.PackageConfig> {
            name = "gradle-api-jars"
            setLabels("gradle", "gradle-api", "gradle-plugins")
            publicDownloadNumbers = true
        })
    }
}