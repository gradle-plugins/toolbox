pluginManagement {
    if (System.getProperty("dev.gradleplugins.useLocal") != null && (System.getProperty("dev.gradleplugins.useLocal") == "true" || System.getProperty("dev.gradleplugins.useLocal") == "")) {

        repositories {
            mavenLocal()
            gradlePluginPortal()
        }

        val version = file("version.txt").readText()
        resolutionStrategy {
            eachPlugin {
                if (requested.id.namespace == "dev.gradleplugins") {
                    useModule("dev.gradleplugins:gradle-plugin-development:${version}")
                }
            }
        }
    }
}


rootProject.name = "toolbox"


// Gradle APIs
include("gradle-api")
project(":gradle-api").projectDir = file("subprojects/gradle-api")
include("gradle-testkit-fixtures")
project(":gradle-testkit-fixtures").projectDir = file("subprojects/gradle-testkit-fixtures")

// Plugins
include("license-gradle-plugin")
project(":license-gradle-plugin").projectDir = file("subprojects/license-gradle-plugin")
include("scm-github-gradle-plugin")
project(":scm-github-gradle-plugin").projectDir = file("subprojects/scm-github-gradle-plugin")

// Plugin developement
include("gradle-plugin-development")
project(":gradle-plugin-development").projectDir = file("subprojects/gradle-plugin-development")
include("gradle-plugin-development-annotation")
project(":gradle-plugin-development-annotation").projectDir = file("subprojects/gradle-plugin-development-annotation")
include("gradle-plugin-development-stubs")
project(":gradle-plugin-development-stubs").projectDir = file("subprojects/gradle-plugin-development-stubs")
